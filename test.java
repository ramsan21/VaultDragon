The issue you’re encountering stems from this exception:

net.minidev.json.parser.ParseException: Malicious payload, having non-natural depths, parsing stopped on: {

This occurs in your TokensBuilder.parse() method (visible in the second image) when calling:

return OIDCTokens.parse((JSONObject) JSON_PARSER.parse(tokens));


⸻

🔍 Root Cause

This error:

Malicious payload, having non-natural depths

is thrown by the net.minidev.json.parser.JSONParser class (from JSON Smart). It usually means:
	•	The token string has unexpected structure (e.g., deeply nested JSON, or incorrect base64 padding/format).
	•	Likely, your tokens string is not valid JSON — probably an empty string, malformed string, or a JWT that’s not decoded.
	•	Since it works right after reboot but fails after high traffic — it could be due to token cache corruption, invalid/incomplete tokens, or memory/GC issues affecting stored values.

⸻

✅ Recommendations

1. Validate tokens string format

Before parsing, log and validate that tokens contains a proper JSON string:

log.info("Raw tokens string: [{}]", tokens);

Add a check:

try {
    Object parsed = JSON_PARSER.parse(tokens);
    if (!(parsed instanceof JSONObject)) {
        log.warn("Parsed token is not a JSONObject");
        return null;
    }
    return OIDCTokens.parse((JSONObject) parsed);
} catch (ParseException e) {
    log.warn("JSON parsing failed for token: {}", tokens, e);
    return null;
}


⸻

2. Check for token caching issues
	•	If tokens come from a cache (as your log Found Token from cache as ... suggests), then ensure concurrent access or eviction doesn’t corrupt it.
	•	Consider deep-copying or serializing/deserializing the token string to isolate memory integrity.

⸻

3. Harden token input at the API boundary

Validate token payload before passing to this utility. If you’re accepting JWTs or OAuth tokens in headers or request bodies, ensure they are base64-decoded and verified correctly.

⸻

4. Mitigate with fallback logic

If this is a transient/corrupt cache issue, you can optionally fall back to requesting a fresh token:

if (parse fails && cacheUsed) {
   invalidate cache;
   fetch fresh token;
}


⸻

5. Increase parser depth limit (not recommended unless verified safe)

If this really is a valid but deeply nested JSON (unlikely for tokens), you can increase the parser limit:

JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
parser.setMaxDepth(2048); // risky unless sure


⸻

🔧 Long-Term Fix
	•	Audit the source of these tokens in the cache.
	•	Monitor the cache size/expiry policy.
	•	Add an automated retry mechanism to refresh corrupt tokens.

⸻

Would you like help reviewing your cache logic or decoding a sample tokens string that caused this error? That might give more clarity.
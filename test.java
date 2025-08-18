You’re matching an attribute (id="...") but in your XML the UUID is inside an element:

<job>
  <id>5967...74698</id>   <!-- this is an element, not id="..." -->
  ...
</job>

That’s why matcher.find() never hits—there’s no id="..." in the content.

Below are two working versions. Use #2 for your file as shown; use #1 only if you really have id="..." attributes elsewhere.

⸻

1) Replace an attribute value: id="..."

import java.util.UUID;
import java.util.regex.*;

static String replaceAttribute(String content, String attr) {
    Pattern p = Pattern.compile("\\b" + Pattern.quote(attr) + "\\s*=\\s*\"([^\"]*)\"");
    Matcher m = p.matcher(content);

    StringBuffer out = new StringBuffer();
    while (m.find()) {
        String newUUID = UUID.randomUUID().toString();
        // Use quoteReplacement in case the value ever has $ or \
        m.appendReplacement(out,
                attr + "=\"" + Matcher.quoteReplacement(newUUID) + "\"");
    }
    m.appendTail(out);
    return out.toString();
}

Usage:

content = replaceAttribute(content, "id");


⸻

2) Replace an element value: <id>...</id>  ✅ (matches your screenshot)

import java.util.UUID;
import java.util.regex.*;

static String replaceElementText(String content, String elementName) {
    Pattern p = Pattern.compile("(<" + Pattern.quote(elementName) + ">)([^<]*)(</"
                                + Pattern.quote(elementName) + ">)");
    Matcher m = p.matcher(content);

    StringBuffer out = new StringBuffer();
    while (m.find()) {
        String newUUID = UUID.randomUUID().toString();
        m.appendReplacement(out, "$1" + Matcher.quoteReplacement(newUUID) + "$3");
    }
    m.appendTail(out);
    return out.toString();
}

Usage:

content = replaceElementText(content, "id");


⸻

Note (robust option)

Regex is fine for a quick edit, but if you can, parse the XML and set the node value:

Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    .parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));

NodeList ids = doc.getElementsByTagName("id");
for (int i = 0; i < ids.getLength(); i++) {
    ids.item(i).setTextContent(UUID.randomUUID().toString());
}
// transform doc back to string...

If you paste one of the snippets above into your method, matcher.find() will work and each <id>/id="..." will be replaced with a fresh UUID.
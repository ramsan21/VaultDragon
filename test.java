SELECT
  REGEXP_SUBSTR(req_body, '"orgId"\s*:\s*"(.*?)",', 1, 1, NULL, 1) AS org_id,
  REGEXP_SUBSTR(req_body, '"profileOrgID"\s*:\s*"(.*?)",', 1, 1, NULL, 1) AS profile_org_id
FROM your_table
WHERE your_condition;

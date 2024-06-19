import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonNodeExample {

    public static void main(String[] args) {
        String jsonString = "{\n" +
                "\t\"request\":{\n" +
                "\t\"adminuser\": {\n" +
                "\t\"appId\": \"IDC\",\n" +
                "\t\"groupId\":\"\",\n" +
                "\t\"userid\":\"\",\n" +
                "\t\"Password\":{\n" +
                "\t\t\"Password\": \"\",\n" +
                "\t\t\"type\":13\n" +
                "\t}\n" +
                "\t},\n" +
                "\t\"users\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"appId\": \"IDC\",\n" +
                "\t\"groupId\":\"\",\n" +
                "\t\"userid\":\"\",\n" +
                "\t\t}\n" +
                "\t]\n" +
                "\t}\n" +
                "}";

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonString);

            // Map for adminuser specific replacement values
            Map<String, String> adminUserReplacements = new HashMap<>();
            adminUserReplacements.put("appId", "adminAppId");
            adminUserReplacements.put("groupId", "adminGroupId");
            adminUserReplacements.put("userid", "adminUserId");

            // Map for user specific replacement values
            Map<String, String> userReplacements = new HashMap<>();
            userReplacements.put("appId", "userAppId");
            userReplacements.put("groupId", "userGroupId");
            userReplacements.put("userid", "userId");

            // Handle adminuser node
            if (root.has("request") && root.get("request").has("adminuser")) {
                replaceAdminUserValues((ObjectNode) root.get("request").get("adminuser"), adminUserReplacements);
            }

            // Handle users array
            if (root.has("request") && root.get("request").has("users")) {
                replaceUserArrayValues((ObjectNode) root.get("request"), userReplacements);
            }

            String updatedJsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            System.out.println(updatedJsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void replaceAdminUserValues(ObjectNode adminUserNode, Map<String, String> replacements) {
        adminUserNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            if (replacements.containsKey(key)) {
                adminUserNode.put(key, replacements.get(key));
            } else if (entry.getValue().isObject()) {
                replaceAdminUserValues((ObjectNode) entry.getValue(), replacements);
            }
        });
    }

    private static void replaceUserArrayValues(ObjectNode requestNode, Map<String, String> replacements) {
        JsonNode usersArray = requestNode.get("users");
        if (usersArray.isArray()) {
            for (JsonNode userNode : usersArray) {
                if (userNode.isObject()) {
                    replaceUserValues((ObjectNode) userNode, replacements);
                }
            }
        }
    }

    private static void replaceUserValues(ObjectNode userNode, Map<String, String> replacements) {
        userNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            if (replacements.containsKey(key)) {
                userNode.put(key, replacements.get(key));
            }
        });
    }
}

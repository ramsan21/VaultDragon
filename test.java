import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RequestPayload {
    private Request request;

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public static class Request {
        private AdminUser adminuser;
        private List<User> users;

        public AdminUser getAdminuser() {
            return adminuser;
        }

        public void setAdminuser(AdminUser adminuser) {
            this.adminuser = adminuser;
        }

        public List<User> getUsers() {
            return users;
        }

        public void setUsers(List<User> users) {
            this.users = users;
        }
    }

    public static class AdminUser {
        private String appId;
        private String groupId;
        private String userid;
        private Password password;

        @JsonProperty("appId")
        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        @JsonProperty("groupId")
        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        @JsonProperty("userid")
        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        @JsonProperty("Password")
        public Password getPassword() {
            return password;
        }

        public void setPassword(Password password) {
            this.password = password;
        }
    }

    public static class Password {
        private String password;
        private int type;

        @JsonProperty("Password")
        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @JsonProperty("type")
        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

    public static class User {
        private String appId;
        private String groupId;
        private String userid;

        @JsonProperty("appId")
        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        @JsonProperty("groupId")
        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        @JsonProperty("userid")
        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }
    }
}

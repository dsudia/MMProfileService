/**
 * Created by davidsudia on 5/24/16.
 */
public class Profile {
    private String email;
    private String displayName;
    private String lastName;
    private String description;
    private String state;
    private String avatarUrl;
    private String followedUser;

    public Profile(String email, String displayName, String lastName, String description, String state, String avatarUrl, String followedUser) {
        this.email = email;
        this.displayName = displayName;
        this.lastName = lastName;
        this.description = description;
        this.state = state;
        this.avatarUrl = avatarUrl;
        this.followedUser = followedUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getFollowedUser() {
        return followedUser;
    }

    public void setFollowedUser(String followedUser) {
        this.followedUser = followedUser;
    }
}

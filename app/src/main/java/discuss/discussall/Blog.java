package discuss.discussall;

public class Blog {

    private String description;
    private String profileimage;
    private String username;
    private String question;
    private String uid;
    private String currenttime;
    private String exam;
    private String postId;

    public Blog()
    {}

    Blog(String description, String profileimage, String username, String question, String uid, String currenttime, String exam, String postId)
    {
        this.description = description;
        this.profileimage = profileimage;
        this.username = username;
        this.question = question;
        this.uid = uid;
        this.currenttime = currenttime;
        this.exam = exam;
        this.postId = postId;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCurrenttime() {
        return currenttime;
    }

    public void setCurrenttime(String currenttime) {
        this.currenttime = currenttime;
    }

    public String getExam() {
        return exam;
    }

    public void setExam(String exam) {
        this.exam = exam;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}

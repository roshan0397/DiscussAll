package discuss.discussall;

public class Comments {

    private String name;
    private String comment;
    private String date;
    private String image;
    private String answer;
    private String commentId;
    private String postId;
    private String uid;

    public Comments()
    {}

    Comments(String name, String comment, String date, String image, String answer, String commentId, String postId, String uid)
    {
        this.name = name;
        this.comment = comment;
        this.date = date;
        this.image = image;
        this.answer = answer;
        this.commentId = commentId;
        this.postId = postId;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

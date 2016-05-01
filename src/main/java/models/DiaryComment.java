package models;

import java.sql.Timestamp;
import javax.persistence.*;

@Entity
public class DiaryComment {
    private Long id;
    private UserTable author;
    private Long diary_id;
    private String content;
    private Timestamp timestamp;

    public DiaryComment() {}

    public DiaryComment(UserTable author, Long diary_id, String content, Timestamp timestamp) {
        this.author = author;
        this.diary_id = diary_id;
        this.content = content;
        this.timestamp = timestamp;
    }

    @Id
    @Column(name = "diarycomment_id")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    public UserTable getAuthor() {
        return author;
    }

    public void setAuthor(UserTable author) {
        this.author = author;
    }

    public Long getDiary_id() {
        return diary_id;
    }

    public void setDiary_id(Long diary_id) {
        this.diary_id = diary_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return the timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}

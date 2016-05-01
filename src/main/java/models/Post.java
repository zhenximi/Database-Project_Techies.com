package models;

import java.sql.Timestamp;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.*;

@Entity
public class Post {
    private Long id;
    private UserTable author;
    private int datatype;
    private String content;
    private int permission;
    private Timestamp timestamp;

    public Post(UserTable author, int datatype, String content, int permission, Timestamp timestamp) {
        this.author = author;
        this.datatype = datatype;
        this.content = content;
        this.permission = permission;
        this.timestamp = timestamp;
    }

    public Post() {}

    /**
     * @return the id
     */
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    //@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="post_id")
    //@SequenceGenerator(name="post_id", sequenceName="post_id", allocationSize=1)
    @Column(name = "post_id")
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the author
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    public UserTable getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(UserTable author) {
        this.author = author;
    }

    /**
     * @return the datatype
     */
    public int getDatatype() {
        return datatype;
    }

    /**
     * @param datatype the datatype to set
     */
    public void setDatatype(int datatype) {
        this.datatype = datatype;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return the permission
     */
    public int getPermission() {
        return permission;
    }

    /**
     * @param permission the permission to set
     */
    public void setPermission(int permission) {
        this.permission = permission;
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

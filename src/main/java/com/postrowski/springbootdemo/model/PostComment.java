package com.postrowski.springbootdemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Builder
//@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PostComment {

    @Id
    @GeneratedValue
    private Long id;

    private String review;

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "post_id")
    private Post post;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PostComment))
            return false;
        return
                id != null &&
                        id.equals(((PostComment) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}

package com.bookapp.model.book;

import com.fasterxml.jackson.annotation.JsonInclude; // Import this
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor; // Import this
import lombok.Data;              // Import this
import lombok.NoArgsConstructor;   // Import this

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Book {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("author")
    private String author;
    @JsonProperty("published_year")
    private Integer publishedYear;
    @JsonProperty("book_summary")
    private String bookSummary;

}
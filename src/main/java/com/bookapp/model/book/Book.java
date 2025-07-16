package com.bookapp.model.book;

import com.fasterxml.jackson.annotation.JsonInclude; // Import this
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor; // Import this
import lombok.Data;              // Import this
import lombok.NoArgsConstructor;   // Import this

@Data // Generates getters, setters, equals, hashCode, toString
@NoArgsConstructor // Generates a public no-argument constructor (essential for Jackson deserialization)
@AllArgsConstructor // Generates a constructor with ALL fields (id, name, author, publishedYear, bookSummary)
@JsonInclude(JsonInclude.Include.NON_NULL) // Only include non-null fields when serializing to JSON
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
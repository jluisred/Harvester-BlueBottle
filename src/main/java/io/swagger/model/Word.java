package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;




/**
 * Word
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-11-02T11:29:00.407Z")

public class Word   {
  private String surface = null;

  private Double score = null;

  public Word surface(String surface) {
    this.surface = surface;
    return this;
  }

   /**
   * Word belonging to a particular topic
   * @return surface
  **/
  @ApiModelProperty(value = "Word belonging to a particular topic")
  public String getSurface() {
    return surface;
  }

  public void setSurface(String surface) {
    this.surface = surface;
  }

  public Word score(Double score) {
    this.score = score;
    return this;
  }

   /**
   * Importancy of the word inside the topic
   * @return score
  **/
  @ApiModelProperty(value = "Importancy of the word inside the topic")
  public Double getScore() {
    return score;
  }

  public void setScore(Double score) {
    this.score = score;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Word word = (Word) o;
    return Objects.equals(this.surface, word.surface) &&
        Objects.equals(this.score, word.score);
  }

  @Override
  public int hashCode() {
    return Objects.hash(surface, score);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Word {\n");
    
    sb.append("    surface: ").append(toIndentedString(surface)).append("\n");
    sb.append("    score: ").append(toIndentedString(score)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}


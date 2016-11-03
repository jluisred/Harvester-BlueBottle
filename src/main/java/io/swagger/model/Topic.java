package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.model.Word;
import java.util.ArrayList;
import java.util.List;




/**
 * Topic
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-11-02T11:29:00.407Z")

public class Topic   {
  private List<Word> topic = new ArrayList<Word>();

  private Double weight = null;

  public Topic topic(List<Word> topic) {
    this.topic = topic;
    return this;
  }

  public Topic addTopicItem(Word topicItem) {
    this.topic.add(topicItem);
    return this;
  }

   /**
   * Word belonging to a particular topic
   * @return topic
  **/
  @ApiModelProperty(value = "Word belonging to a particular topic")
  public List<Word> getTopic() {
    return topic;
  }

  public void setTopic(List<Word> topic) {
    this.topic = topic;
  }

  public Topic weight(Double weight) {
    this.weight = weight;
    return this;
  }

   /**
   * Importancy of the topic inside the document
   * @return weight
  **/
  @ApiModelProperty(value = "Importancy of the topic inside the document")
  public Double getWeight() {
    return weight;
  }

  public void setWeight(Double weight) {
    this.weight = weight;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Topic topic = (Topic) o;
    return Objects.equals(this.topic, topic.topic) &&
        Objects.equals(this.weight, topic.weight);
  }

  @Override
  public int hashCode() {
    return Objects.hash(topic, weight);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Topic {\n");
    
    sb.append("    topic: ").append(toIndentedString(topic)).append("\n");
    sb.append("    weight: ").append(toIndentedString(weight)).append("\n");
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


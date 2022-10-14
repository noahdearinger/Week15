/**
 * 
 */
package com.promineotech.jeep.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import com.promineotech.jeep.Constants;
import com.promineotech.jeep.JeepSales;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;
import lombok.Getter;

/**
 * @author smith
 *
 */

// @formatter:off
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = {JeepSales.class})
@ActiveProfiles("test")
@Sql(scripts = {
    "classpath:flyway/migrations/V1.0__Jeep_Schema.sql",
    "classpath:flyway/migrations/V1.1__Jeep_Data.sql"
    },
 config = @SqlConfig(encoding = "utf-8"))
//@formatter:on

class FetchJeepTest {
@Getter
@Autowired
private TestRestTemplate restTemplate;

@LocalServerPort
private int serverPort;
  @Test
  void testThatJeepsAreReturnedWhenAValidModelAndTrimAreSupplied() {
   JeepModel model = JeepModel.WRANGLER;
   String trim = "Sport";
// @formatter:off
   String uri = String.format("http://localhost:%d/jeeps?model=%s&trim=%s", 
       serverPort, model, trim);
   
   //http request to rest service
   ResponseEntity<List<Jeep>> response = getRestTemplate().exchange(uri, HttpMethod.GET,
       null, new ParameterizedTypeReference<List<Jeep>>() {});
// @formatter:on
   
   assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
   
   List<Jeep> actual = response.getBody();
   
   List<Jeep> expected = buildExpected();
   
   assertThat(actual).isEqualTo(expected);
  }
  /**
   * @return
   */
  protected List<Jeep> buildExpected() {
  List<Jeep> list = new LinkedList<>();
  // @formatter:off
 
      list.add(Jeep.builder()
          .modelId(JeepModel.WRANGLER)
          .trimLevel("Sport")
          .numDoors(4)
          .wheelSize(17)
          .basePrice(new BigDecimal("31975.00"))
            .build());
      
      list.add(Jeep.builder()
          .modelId(JeepModel.WRANGLER)
          .trimLevel("Sport")
          .numDoors(2)
          .wheelSize(17)
          .basePrice(new BigDecimal("28475.00"))
            .build());
             
     // @formatter:on    
     
  Collections.sort(list);
  return list;
  }
  
  @Test
  void testThatAnErrorMessageIsReturnedWhenAnUnknownTrimIsSupplied() {
  
    //Given: an invalid trim level
    JeepModel model = JeepModel.WRANGLER;
   String trim = "Unknown value";
// @formatter:off
   String uri = String.format("http://localhost:%d/jeeps?model=%s&trim=%s", 
       serverPort, model, trim);
   System.out.println(uri);
   
   // When: a connection is made to URI
   //http request to rest service
   ResponseEntity<Map<String, Object>> response = getRestTemplate().exchange(uri, HttpMethod.GET,
       null, new ParameterizedTypeReference<Map<String, Object>>() {});
// @formatter:on
   System.out.println(response.getBody());
   // Then: a not found (404) action is returned
   assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
   
   // And: an error message is returned
 Map<String, Object> error = response.getBody();
 
 assertErrorMessageValid(error, HttpStatus.NOT_FOUND);
  }

  @ParameterizedTest
  @MethodSource("com.promineotech.jeep.controller.FetchJeepTest#parametersForInvalidInput")
  void testThatAnErrorMessageIsReturnedWhenAnInvalidValueIsSupplied(
      String model, String trim, String reason) {
  
    //Given: an invalid trim level
 
// @formatter:off
   String uri = String.format("http://localhost:%d/jeeps?model=%s&trim=%s", 
       serverPort, model, trim);
   
   // When: a connection is made to URI
   //http request to rest service
   ResponseEntity<Map<String, Object>> response = restTemplate.exchange(uri, HttpMethod.GET,
       null, new ParameterizedTypeReference<>() {});
// @formatter:on
   
   // Then: a not found (404) action is returned
   assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
   
   // And: an error message is returned
 Map<String, Object> error = response.getBody();
 
 assertErrorMessageValid(error, HttpStatus.BAD_REQUEST);
  }
 
  protected void assertErrorMessageValid(Map<String, Object> error, HttpStatus status) {
    // formatter:off
     assertThat(error)
       .containsKey("message")
       .containsEntry("status code", status.value())
       .containsEntry("uri", "/jeeps")
       .containsKey("timestamp")
       .containsEntry("reason", status.getReasonPhrase());
     // formatter:on
  } 
 
  static Stream<Arguments> parametersForInvalidInput() {
    return Stream.of(
          arguments("WRANGLER", "@#$*", "Trim contains non aplha-numeric characters."),
          arguments("WRANGLER", "C".repeat(Constants.TRIM_MAX_LENGTH + 1), "Trim character length too long."),
          arguments("INVALID", "Sport", "model not in enum.")
          
        );
        
  }
  
}

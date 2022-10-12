/**
 * 
 */
package com.promineotech.jeep.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;
import com.promineotech.jeep.service.DefaultJeepSalesService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author smith
 *
 */
@Slf4j
@RestController

public class DefaultJeepSalesController implements JeepSalesController {

  @Autowired
  private DefaultJeepSalesService jeepSalesService;
  
  
  @Override
  public List<Jeep> fetchJeeps(JeepModel model, String trim) {
    log.info("model={}&trim={}", model, trim);
    return jeepSalesService.fetchJeeps(JeepModel.WRANGLER, "Sport");
 
  }

}

package com.adfarms.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@ControllerAdvice(annotations = {Controller.class})
public class CustomErrorHandling {
    public static final String ERROR_VIEW = "error";

    @ExceptionHandler({Exception.class})
    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception exception) {
        String errorMessage = null;

        ModelAndView errPage = new ModelAndView();
        errPage.setViewName(ERROR_VIEW);
        if(exception.getMessage() != null){
            errorMessage = exception.getMessage();
        } else if(exception.getCause() != null){
            errorMessage = exception.getCause().getMessage();
        } else {
            errorMessage = exception.toString();
        }

        errPage.addObject("errMsg", errorMessage);
        log.error("Error: ", exception);
       return errPage;
    }

}

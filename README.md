<!--
  Title: SAML proxy + Spring Boot + Thymeleaf + Bootstrap
  Description: SAML proxy application to be deployed as a downstream service in front of a legacy SAML application. The application configuration can be changed and applied at runtime
  Author: Dmitry Losev
  -->

# SAML proxy + Spring Boot + Thymeleaf + Bootstrap

This project implements SAML proxy-service for one of my customers. It's designed to be a downstream service in front of a legacy SAML application 
(wich can't be modified). The application configuration, including Idp and SP settings, logging level, etc can be modified and applied at runtime without
reload.

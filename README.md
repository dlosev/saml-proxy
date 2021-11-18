TODO:
1. Admin UI interface to set SP metadata settings;
2. Admin UI allows uploading of new private key and certificates for SP;
3. Investigate this error and understand how to avoid it:
    ```bash
   DEBUG | 2021-10-26 10:36:57,696 | WebSSOProfileConsumerImpl.processAuthenticationResponse | Validation of authentication statement in assertion failed, skipping
   org.springframework.security.authentication.CredentialsExpiredException: Authentication statement is too old to be used with value 2021-10-08T17:57:33.898Z
    ```
4. Logging level can be configured and logs can be downloaded via admin UI;
5. Configuration must be dynamically updated for the running app after admin changes it; 
6. Save configuration on disk;
7. Save http sessions on disk
8. Resolve first run issue. When config file is not yet created, but beans can't initialize, because some properties are missing  


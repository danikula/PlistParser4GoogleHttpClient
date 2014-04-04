PlistParser4GoogleHttpClient
============================

###What is it?
PlistParser4GoogleHttpClient is SAX based pluggable streaming [plist](http://en.wikipedia.org/wiki/Property_list) parser for [Android Google Http Client](https://code.google.com/p/google-http-java-client).

###Setup
Download [jar](https://github.com/danikula/PlistParser4GoogleHttpClient/blob/master/PlistParser4GoogleHttpClient-0.1.jar) and add it as dependency to your android project. The minimal supported version of android API is 8.

### How to use?
Imagine some API return response in plist format:
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
    <dict>
        <key>name</key>
        <string>Alexey</string>
        <key>age</key>
        <integer>26</integer>
        <key>birthday</key>
        <date>1986-12-31T10:10:10</date>
        <key>devices</key>
        <array>
            <string>HTC Desire S</string>
            <string>Google Nexus 7</string>
            <string/>
        </array>
    </dict>
</plist>        
```

Corresponding POJO for this repsonse is:
```
public class User {

    @Key("name")
    private String name;

    @Key("age")
    private int age;

    @Key("birthday")
    private Date birthday;

    @Key("devices")
    private List<String> devices;
    
    // No setters are needed, because Google Http Client uses reflection to fill POJO. 
    // getters for fields
    // ..
}    
```

To parse this response into POJO use something like this:
```
HttpTransport httpTransport = new NetHttpTransport();
HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(url));
request.setParser(new PlistObjectParser());
User user = request.parseAs(User.class);
```
See more interesting [examples from Google](https://code.google.com/p/google-http-java-client/wiki/Samples).




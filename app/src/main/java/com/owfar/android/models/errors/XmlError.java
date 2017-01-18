package com.owfar.android.models.errors;

public class XmlError {

//    @XmlElement(name = "Code")
//    private String code;
//
//    @XmlElement(name = "Message")
//    private String message;
//
//    @XmlElement(name = "Expires")
//    @XmlJavaTypeAdapter(DateAdapter.class)
//    private Date expires;
//
//    @XmlElement(name = "ServerTime")
//    @XmlJavaTypeAdapter(DateAdapter.class)
//    private Date serverTime;
//
//    @XmlElement(name = "RequestId")
//    private String requestId;
//
//    @XmlElement(name = "HostId")
//    private String hostId;

    public static XmlError parseDemo() {
        final String xmlString = "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Error>\n" +
                "    <Code>AccessDenied</Code>\n" +
                "    <Message>Request has expired</Message>\n" +
                "    <Expires>2016-11-01T14:18:07Z</Expires>\n" +
                "    <ServerTime>2016-11-01T14:56:09Z</ServerTime>\n" +
                "    <RequestId>3BA0B47F86A1C500</RequestId>\n" +
                "    <HostId>HRusCT9wXCpmo3nctK/mrpGC4iJwMY6GMGEVruVZlx6eeBt1ir9kjTvE7AP+m1f55boK5fgYFeY=</HostId>\n" +
                "</Error>";
//        try {
//            JAXBContext jaxbContext = JAXBContext.newInstance(XmlError.class);
//            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//            StringReader stringReader = new StringReader(xmlString);
//            XmlError xmlError = (XmlError) unmarshaller.unmarshal(stringReader);
//            return xmlError;
//        } catch (JAXBException e) {
//            e.printStackTrace();
            return null;
//        }
    }

//    @Override
//    public String toString() {
//        final StringBuilder sb = new StringBuilder("XmlError{");
//        sb.append("code='").append(code).append('\'');
//        sb.append(", message='").append(message).append('\'');
//        sb.append(", expires=").append(expires);
//        sb.append(", serverTime=").append(serverTime);
//        sb.append(", requestId='").append(requestId).append('\'');
//        sb.append(", hostId='").append(hostId).append('\'');
//        sb.append('}');
//        return sb.toString();
//    }
//
//    private class DateAdapter extends XmlAdapter<String, Date> {
//
//        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
//
//        @Override
//        public String marshal(Date v) throws Exception {
//            synchronized (dateFormat) {
//                return dateFormat.format(v);
//            }
//        }
//
//        @Override
//        public Date unmarshal(String v) throws Exception {
//            synchronized (dateFormat) {
//                return dateFormat.parse(v);
//            }
//        }
//    }
}

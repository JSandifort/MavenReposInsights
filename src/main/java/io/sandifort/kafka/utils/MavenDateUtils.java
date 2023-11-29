package io.sandifort.kafka.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.jsoup.Jsoup;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MavenDateUtils {

    /**
     * Fetches the date when a specific Maven artifact was last updated.
     *
     * @param groupId    The group ID of the Maven artifact.
     * @param artifactId The artifact ID of the Maven artifact.
     * @param version    The version of the Maven artifact.
     * @return The last updated date as a String.
     */
    public static String getLastUpdatedDate(String groupId, String artifactId, String version) {
        try {
            String formattedGroupId = groupId.replace('.', '/');
            String url = String.format("https://repo1.maven.org/maven2/%s/%s/%s/", formattedGroupId, artifactId, version);

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                org.w3c.dom.Document doc = builder.parse(conn.getInputStream());
                conn.disconnect();

                XPathFactory xpathFactory = XPathFactory.newInstance();
                XPath xpath = xpathFactory.newXPath();
                return xpath.evaluate("/metadata/versioning/lastUpdated", doc);
            } else {
                System.out.println("Failed to fetch metadata: HTTP error code " + conn.getResponseCode());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String fetchArtifactVersionDetails(String groupId, String artifactId, String version) throws IOException {
        String formattedGroupId = groupId.replace('.', '/');
        String url = String.format("https://repo1.maven.org/maven2/%s/%s/%s/", formattedGroupId, artifactId, version);

        Document doc = Jsoup.parse(new URL(url), 10000); // timeout set to 10000 milliseconds
        Element contents = doc.selectFirst("#contents");
        Elements links = contents.select("a[href="+ artifactId + "-" + version + ".pom" +"]");

        // Assuming there's only one such element
        if (!links.isEmpty()) {
            Element link = links.first();

            // Navigate to the next sibling (which is a text node) and get its whole text
            String text = link.nextSibling().outerHtml().trim();

            // Extract the date part from the text
            String date = text.split("\\s+")[0]; // Split by whitespace and take the first part - yyyy-MM-dd format

            System.out.println("Date: " + date);
            return date;
        }
//        for (Element link : links) {
//            String fileName = link.text();
//            if (!fileName.equals("../")) {
//                String fileDetails = link.parent().ownText().trim();
//                System.out.println(fileName + " - " + fileDetails);
//            }
//        }

        return null;
    }
}

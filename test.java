You’re seeing two things DOM normally does when you “round-trip” an XML:
	1.	A new first line (the XML declaration) — because your transformer is set to OMIT_XML_DECLARATION = “no”
	2.	Re-indentation / spacing changes — because INDENT = “yes” (and your indent-amount property is also malformed; it must be "{http://xml.apache.org/xslt}indent-amount").

If you want to only change the id/uuid values and keep the original layout, write the DOM back without reformatting and (optionally) omit the declaration. Also remove any leading whitespace text node before the root, which can cause a blank line.

Drop-in code:

private static void replaceNodeInFile(Path file) {
    try {
        System.out.println("Processing file: " + file);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        // keep whitespace exactly as is
        dbf.setIgnoringElementContentWhitespace(false);

        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(file.toFile());

        // update attributes wherever they appear
        String newUUID = UUID.randomUUID().toString();
        replaceAttr(doc, "id", newUUID);
        replaceAttr(doc, "uuid", newUUID);

        // if there is an initial whitespace text node, remove it to avoid extra blank line
        Node first = doc.getFirstChild();
        if (first != null && first.getNodeType() == Node.TEXT_NODE
                && first.getTextContent().trim().isEmpty()) {
            doc.removeChild(first);
        }

        // write back WITHOUT reformatting and WITHOUT adding XML declaration
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.METHOD, "xml");
        t.setOutputProperty(OutputKeys.INDENT, "no");
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // set "no" if you actually want it
        // If you ever want pretty print: set INDENT="yes" and
        // t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        t.transform(new DOMSource(doc), new StreamResult(file.toFile()));
    } catch (Exception e) {
        System.err.println("Error processing file: " + file + " -> " + e.getMessage());
    }
}

private static void replaceAttr(Document doc, String attrName, String newVal) {
    NodeList nodes = doc.getElementsByTagName("*");
    for (int i = 0; i < nodes.getLength(); i++) {
        Element el = (Element) nodes.item(i);
        if (el.hasAttribute(attrName)) {
            el.setAttribute(attrName, newVal);
        }
    }
}

Key points to fix your issue:
	•	Use OutputKeys.INDENT = "no" to avoid reformatting (“orientation” changes).
	•	Use OutputKeys.OMIT_XML_DECLARATION = "yes" if you don’t want the extra first line.
	•	If you do pretty-print later, the indent amount property must be "{http://xml.apache.org/xslt}indent-amount".
	•	Remove a leading whitespace text node to prevent a blank line before the root element.

This will update id/uuid and keep the rest of the file the same.
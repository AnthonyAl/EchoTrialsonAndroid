package com.unipi.alexandris.android.echotrialsonandroid.controller;

import android.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * DataCompressionHelper handles XML compression and decompression for level data.
 * 
 * <h3>Compression Strategy:</h3>
 * <ul>
 *   <li><b>Blocks Only:</b> Compresses repetitive block elements by grouping by type, size, and groupId</li>
 *   <li><b>Position Compression:</b> Converts individual block positions to compressed position strings</li>
 *   <li><b>Other Objects:</b> Leaves players, triggers, and other unique objects uncompressed</li>
 * </ul>
 * 
 * <h3>Compressed Format:</h3>
 * <pre>
 * &lt;blocks type="SimpleBlockCommon" size="48"&gt;
 *   &lt;positions groupId="0"&gt;2832,4080;2880,3552;2832,4032;...&lt;/positions&gt;
 *   &lt;positions groupId="1"&gt;2833,4080;2881,3552;2833,4032;...&lt;/positions&gt;
 * &lt;/blocks&gt;
 * </pre>
 * 
 * <h3>Usage:</h3>
 * <ol>
 *   <li><b>Compression:</b> Call compressXML() before saving to Firebase</li>
 *   <li><b>Decompression:</b> Call decompressXML() before passing to DataDecoder</li>
 * </ol>
 */
public class DataCompressionHelper {
    private static final String TAG = "DataCompressionHelper";
    
    /**
     * Compresses XML document by grouping repetitive block elements.
     * Only compresses blocks - leaves other objects unchanged.
     * 
     * @param document The XML document to compress (will be modified in-place)
     * @return The same document (now compressed) or null if compression failed
     */
    public Document compressXML(Document document) {
        if (document == null) {
            Log.e(TAG, "Cannot compress null document");
            return null;
        }
        
        try {
            Element objectsElement = findObjectsElement(document);
            if (objectsElement == null) {
                Log.w(TAG, "No objects element found, returning original document");
                return document;
            }

            compressBlocksInElement(objectsElement);
            addCompressionMetadata(document);
            
            return document;
            
        } catch (Exception e) {
            Log.e(TAG, "Error compressing XML: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Decompresses XML document by expanding compressed block elements.
     * Converts compressed blocks back to individual block elements for DataDecoder.
     * 
     * @param document The compressed XML document
     * @return The decompressed XML document ready for DataDecoder
     */
    public Document decompressXML(Document document) {
        if (document == null) {
            Log.e(TAG, "Cannot decompress null document");
            return null;
        }
        
        try {
            if (!isDocumentCompressed(document)) {
                return document;
            }

            Document decompressedDoc = cloneDocument(document);

            Element objectsElement = findObjectsElement(decompressedDoc);
            if (objectsElement == null) {
                Log.w(TAG, "No objects element found, returning original document");
                return decompressedDoc;
            }

            decompressBlocksInElement(objectsElement);
            removeCompressionMetadata(decompressedDoc);
            

            return decompressedDoc;
            
        } catch (Exception e) {
            Log.e(TAG, "Error decompressing XML: " + e.getMessage(), e);
            return document;
        }
    }
    
    /**
     * Checks if a document contains compressed data.
     * A document is considered compressed if it contains <blocks> elements with compressed format:
     * - <blocks> with type and size attributes
     * - Containing <positions> elements instead of individual <block> elements
     * 
     * @param document The document to check
     * @return true if the document contains compressed data, false otherwise
     */
    private boolean isDocumentCompressed(Document document) {
        if (document == null) return false;

        NodeList compressionNodes = document.getElementsByTagName("compressed");
        if (compressionNodes.getLength() > 0) {
            String compressionValue = compressionNodes.item(0).getTextContent();
            return "true".equalsIgnoreCase(compressionValue);
        }

        NodeList blocksElements = document.getElementsByTagName("blocks");
        for (int i = 0; i < blocksElements.getLength(); i++) {
            Element blocksElement = (Element) blocksElements.item(i);

            if (blocksElement.hasAttribute("type") && blocksElement.hasAttribute("size")) {
                NodeList positions = blocksElement.getElementsByTagName("positions");
                NodeList individualBlocks = blocksElement.getElementsByTagName("block");
                
                if (positions.getLength() > 0 && individualBlocks.getLength() == 0) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private void compressBlocksInElement(Element parentElement) {
        NodeList blockNodes = parentElement.getElementsByTagName("block");
        
        if (blockNodes.getLength() == 0) {
            return;
        }

        List<Element> blockElements = new ArrayList<>();
        for (int i = 0; i < blockNodes.getLength(); i++) {
            blockElements.add((Element) blockNodes.item(i));
        }

        Map<String, List<Element>> blockGroups = new HashMap<>();
        
        for (int i = 0; i < blockElements.size(); i++) {
            Element blockElement = blockElements.get(i);
            String compressionKey = createCompressionKey(blockElement);
            
            if (compressionKey != null) {
                blockGroups.computeIfAbsent(compressionKey, k -> new ArrayList<>()).add(blockElement);
            }
        }
        for (Map.Entry<String, List<Element>> entry : blockGroups.entrySet()) {
            List<Element> blocks = entry.getValue();
            
            if (blocks.size() > 1) {
                Element compressedBlock = createCompressedBlockElement(blocks);
                
                if (compressedBlock != null) {
                    for (Element block : blocks) {
                        try {
                            Node actualParent = block.getParentNode();
                            if (actualParent != null) {
                                actualParent.removeChild(block);
                            }
                        } catch (DOMException e) {
                            Log.w(TAG, "Failed to remove block: " + e.getMessage());
                        }
                    }
                    parentElement.appendChild(compressedBlock);
                }
            }
        }
        

    }

    private void decompressBlocksInElement(Element parentElement) {
        NodeList compressedBlockNodes = parentElement.getElementsByTagName("blocks");
        
        for (int i = 0; i < compressedBlockNodes.getLength(); i++) {
            Element compressedBlock = (Element) compressedBlockNodes.item(i);

            if (!compressedBlock.hasAttribute("type") || !compressedBlock.hasAttribute("size")) {
                continue;
            }

            List<Element> individualBlocks = expandCompressedBlock(compressedBlock);
            for (Element individualBlock : individualBlocks) {
                parentElement.insertBefore(individualBlock, compressedBlock);
            }
            parentElement.removeChild(compressedBlock);
        }
    }

    private String createCompressionKey(Element blockElement) {
        String type = blockElement.getAttribute("type");
        if (type.isEmpty()) {
            Log.w(TAG, "Block element missing type attribute");
            return null;
        }
        
        String size = getElementTextContent(blockElement, "size");
        if (size.isEmpty()) {
            Log.w(TAG, "Block element missing size element");
            return null;
        }
        
        String groupId = getElementTextContent(blockElement, "groupId");
        if (groupId.isEmpty()) {
            Log.w(TAG, "Block element missing groupId element");
            return null;
        }
        
        return type + ":" + size + ":" + groupId;
    }

    private Element createCompressedBlockElement(List<Element> blocks) {
        if (blocks.isEmpty()) return null;
        
        Element firstBlock = blocks.get(0);
        Document doc = firstBlock.getOwnerDocument();

        Element compressedBlock = doc.createElement("blocks");
        compressedBlock.setAttribute("type", firstBlock.getAttribute("type"));
        compressedBlock.setAttribute("size", getElementTextContent(firstBlock, "size"));

        Map<String, List<String>> positionsByGroup = new HashMap<>();
        
        for (Element block : blocks) {
            String groupId = getElementTextContent(block, "groupId");
            String position = extractPositionFromBlock(block);
            
            if (position != null) {
                positionsByGroup.computeIfAbsent(groupId, k -> new ArrayList<>()).add(position);
            }
        }

        for (Map.Entry<String, List<String>> entry : positionsByGroup.entrySet()) {
            Element positionsElement = doc.createElement("positions");
            positionsElement.setAttribute("groupId", entry.getKey());

            String positionsString = String.join(";", entry.getValue());
            Text positionsText = doc.createTextNode(positionsString);
            positionsElement.appendChild(positionsText);
            
            compressedBlock.appendChild(positionsElement);
        }
        
        return compressedBlock;
    }

    private List<Element> expandCompressedBlock(Element compressedBlock) {
        List<Element> individualBlocks = new ArrayList<>();
        Document doc = compressedBlock.getOwnerDocument();
        
        String type = compressedBlock.getAttribute("type");
        String size = compressedBlock.getAttribute("size");

        NodeList positionGroups = compressedBlock.getElementsByTagName("positions");
        
        for (int i = 0; i < positionGroups.getLength(); i++) {
            Element positionGroup = (Element) positionGroups.item(i);
            String groupId = positionGroup.getAttribute("groupId");
            String positionsString = positionGroup.getTextContent();

            String[] positions = positionsString.split(";");
            
            for (String position : positions) {
                if (position.trim().isEmpty()) continue;

                Element individualBlock = createIndividualBlockElement(doc, type, size, groupId, position);
                if (individualBlock != null) {
                    individualBlocks.add(individualBlock);
                }
            }
        }
        
        return individualBlocks;
    }

    private Element createIndividualBlockElement(Document doc, String type, String size, String groupId, String position) {
        Element block = doc.createElement("block");
        block.setAttribute("type", type);

        String[] coords = position.split(",");
        if (coords.length != 2) {
            return null;
        }
        
        String x = coords[0].trim();
        String y = coords[1].trim();

        Element positionElement = doc.createElement("position");
        positionElement.setAttribute("x", x);
        positionElement.setAttribute("y", y);
        block.appendChild(positionElement);

        Element sizeElement = doc.createElement("size");
        sizeElement.setTextContent(size);
        block.appendChild(sizeElement);

        Element groupIdElement = doc.createElement("groupId");
        groupIdElement.setTextContent(groupId);
        block.appendChild(groupIdElement);

        Element physicsElement = doc.createElement("physics");
        addPhysicsProperty(doc, physicsElement, "velX", "0.0");
        addPhysicsProperty(doc, physicsElement, "velY", "0.0");
        addPhysicsProperty(doc, physicsElement, "width", size);
        addPhysicsProperty(doc, physicsElement, "height", size);
        addPhysicsProperty(doc, physicsElement, "widthMultiplier", "1.0");
        addPhysicsProperty(doc, physicsElement, "heightMultiplier", "1.0");
        addPhysicsProperty(doc, physicsElement, "active", "true");
        addPhysicsProperty(doc, physicsElement, "visible", "true");
        block.appendChild(physicsElement);

        Element regionElement = doc.createElement("region");
        Element boundsElement = doc.createElement("bounds");
        String[] coords2 = position.split(",");
        if (coords2.length == 2) {
            try {
                float xPos = Float.parseFloat(coords2[0].trim());
                float yPos = Float.parseFloat(coords2[1].trim());
                int sizeInt = Integer.parseInt(size);
                
                boundsElement.setAttribute("left", String.valueOf((int)xPos));
                boundsElement.setAttribute("top", String.valueOf((int)yPos));
                boundsElement.setAttribute("right", String.valueOf((int)(xPos + sizeInt)));
                boundsElement.setAttribute("bottom", String.valueOf((int)(yPos + sizeInt)));
            } catch (NumberFormatException e) {
                boundsElement.setAttribute("left", "0");
                boundsElement.setAttribute("top", "0");
                boundsElement.setAttribute("right", size);
                boundsElement.setAttribute("bottom", size);
            }
        }
        regionElement.appendChild(boundsElement);
        block.appendChild(regionElement);
        
        return block;
    }

    private void addPhysicsProperty(Document doc, Element physicsElement, String name, String value) {
        Element propertyElement = doc.createElement(name);
        propertyElement.setTextContent(value);
        physicsElement.appendChild(propertyElement);
    }

    private String getElementTextContent(Element parent, String childName) {
        NodeList children = parent.getElementsByTagName(childName);
        if (children.getLength() > 0) {
            return children.item(0).getTextContent();
        }
        return "";
    }

    private String extractPositionFromBlock(Element block) {
        NodeList positionNodes = block.getElementsByTagName("position");
        if (positionNodes.getLength() > 0) {
            Element positionElement = (Element) positionNodes.item(0);
            String x = positionElement.getAttribute("x");
            String y = positionElement.getAttribute("y");
            if (!x.isEmpty() && !y.isEmpty()) {
                return x + "," + y;
            }
        }
        return null;
    }

    private Element findObjectsElement(Document document) {
        NodeList objectsList = document.getElementsByTagName("objects");
        if (objectsList.getLength() > 0) {
            return (Element) objectsList.item(0);
        }
        return null;
    }

    private void addCompressionMetadata(Document document) {
        Element compressedElement = document.createElement("compressed");
        compressedElement.setTextContent("true");
        document.getDocumentElement().appendChild(compressedElement);
    }

    private void removeCompressionMetadata(Document document) {
        NodeList compressedNodes = document.getElementsByTagName("compressed");
        if (compressedNodes.getLength() > 0) {
            Node compressedNode = compressedNodes.item(0);
            compressedNode.getParentNode().removeChild(compressedNode);
        }
    }

    private Document cloneDocument(Document original) throws Exception {
        if (original == null) {
            throw new IllegalArgumentException("Cannot clone null document");
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(original), new StreamResult(outputStream));
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        return builder.parse(inputStream);
    }

    public String documentToString(Document document) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "no");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "UTF-8");
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(document), new StreamResult(outputStream));
            
            return outputStream.toString("UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to string: " + e.getMessage());
            return "Error converting document to string";
        }
    }
}

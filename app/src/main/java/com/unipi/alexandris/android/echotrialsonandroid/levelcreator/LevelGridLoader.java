package com.unipi.alexandris.android.echotrialsonandroid.levelcreator;

import android.content.Context;
import android.util.Log;

import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;

import com.unipi.alexandris.android.echotrialsonandroid.controller.DataCompressionHelper;

/**
 * LevelGridLoader class for loading levels from assets/levels directory into the LevelGridView.
 * <br>
 * This class allows the LevelCreator to load existing levels for editing purposes.
 * It parses XML level files and converts them back to SimpleModel objects for the grid.
 * 
 * <h3>Usage:</h3>
 * <pre>
 * LevelGridLoader loader = new LevelGridLoader(context);
 * boolean success = loader.loadLevelToGrid("tutorial_level_1.xml", gridView);
 * </pre>
 */
public class LevelGridLoader {
    private static final String TAG = "LevelGridLoader";
    private final Context context;
    private final DataCompressionHelper compressionHelper;

    public LevelGridLoader(Context context) {
        this.context = context;
        this.compressionHelper = new DataCompressionHelper();
    }

    /**
     * Loads a level from R class resources into the specified LevelGridView.
     * 
     * @param levelResourceId The R class resource ID of the level to load
     * @param gridView The LevelGridView to populate with the loaded level data
     * @return true if the level was successfully loaded, false otherwise
     */
    public boolean loadLevelToGrid(int levelResourceId, LevelGridView gridView) {
        try {
            Log.d(TAG, "Starting to load level with resource ID: " + levelResourceId);
            
            // Load XML document from resources
            Document document = loadXMLFromAssets(levelResourceId);
            if (document == null) {
                Log.e(TAG, "Failed to load XML document for resource ID: " + levelResourceId);
                return false;
            }
            
            Log.d(TAG, "XML document loaded successfully, clearing grid");

            // Clear the current grid
            gridView.clearGrid();

            // Parse level data and populate grid
            boolean parseSuccess = parseLevelData(document, gridView);
            
            Log.d(TAG, "Level parsing completed with result: " + parseSuccess);
            return parseSuccess;

        } catch (Exception e) {
            Log.e(TAG, "Error loading level to grid for resource ID: " + levelResourceId, e);
            return false;
        }
    }
    
    /**
     * Loads a level from XML string into the specified LevelGridView.
     * 
     * @param levelDataString The XML string containing the level data
     * @param gridView The LevelGridView to populate with the loaded level data
     * @return true if the level was successfully loaded, false otherwise
     */
    public boolean loadLevelFromString(String levelDataString, LevelGridView gridView) {
        try {
            Log.d(TAG, "Starting to load level from string");
            
            // Load XML document from string
            Document document = loadXMLFromString(levelDataString);
            if (document == null) {
                Log.e(TAG, "Failed to load XML document from string");
                return false;
            }
            
            Log.d(TAG, "XML document loaded successfully from string, clearing grid");

            // Clear the current grid
            gridView.clearGrid();

            // Parse level data and populate grid
            boolean parseSuccess = parseLevelData(document, gridView);
            
            Log.d(TAG, "Level parsing from string completed with result: " + parseSuccess);
            return parseSuccess;

        } catch (Exception e) {
            Log.e(TAG, "Error loading level from string", e);
            return false;
        }
    }
    
    /**
     * Loads an XML document from the R class resources.
     * 
     * @param resourceId The R class resource ID to load
     * @return The parsed Document, or null if loading failed
     */
    private Document loadXMLFromAssets(int resourceId) {
        try {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            inputStream.close();
            
            // Decompress the document if it contains compressed data
                    Document decompressedDocument = compressionHelper.decompressXML(document);
        if (decompressedDocument == null) {
            Log.w(TAG, "Decompression failed, using original document");
            decompressedDocument = document;
        }
            
            return decompressedDocument;
        } catch (Exception e) {
            Log.e(TAG, "Error loading XML from resources: " + resourceId, e);
            return null;
        }
    }
    
    /**
     * Loads an XML document from a string.
     * 
     * @param xmlString The XML string to parse
     * @return The parsed Document, or null if loading failed
     */
    private Document loadXMLFromString(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(xmlString));
            Document document = builder.parse(inputSource);
            
            // Decompress the document if it contains compressed data
            Document decompressedDocument = compressionHelper.decompressXML(document);
            if (decompressedDocument == null) {
                Log.w(TAG, "Decompression failed, using original document");
                decompressedDocument = document;
            }
            
            return decompressedDocument;
        } catch (Exception e) {
            Log.e(TAG, "Error loading XML from string", e);
            return null;
        }
    }
    
    /**
     * Parses the level data from XML and populates the grid view.
     * 
     * @param document The XML document to parse
     * @param gridView The grid view to populate
     * @return true if parsing was successful
     */
    private boolean parseLevelData(Document document, LevelGridView gridView) {
        try {
            // Get the root element
            Element rootElement = document.getDocumentElement();
            if (!rootElement.getTagName().equals("level")) {
                Log.e(TAG, "Invalid level XML: root element is not 'level'");
                return false;
            }

            // Get grid size from metadata
            int gridSize = getGridSizeFromMetadata(rootElement);
            gridView.setCellSize(gridSize);

            // Parse objects section
            NodeList objectsNodes = rootElement.getElementsByTagName("objects");
            if (objectsNodes.getLength() > 0) {
                Element objectsElement = (Element) objectsNodes.item(0);
                parseObjectsSection(objectsElement, gridView, gridSize);
            }

            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing level data", e);
            return false;
        }
    }

    /**
     * Gets the grid size from the metadata section.
     * 
     * @param rootElement The root element of the XML
     * @return The grid size, or 32 as default
     */
    private int getGridSizeFromMetadata(Element rootElement) {
        NodeList metadataNodes = rootElement.getElementsByTagName("metadata");
        if (metadataNodes.getLength() > 0) {
            Element metadataElement = (Element) metadataNodes.item(0);
            NodeList gridSizeNodes = metadataElement.getElementsByTagName("gridSize");
            if (gridSizeNodes.getLength() > 0) {
                return Integer.parseInt(gridSizeNodes.item(0).getTextContent());
            }
        }
        return 32; // Default grid size
    }

    /**
     * Parses the objects section and populates the grid.
     * 
     * @param objectsElement The objects element from XML
     * @param gridView The grid view to populate
     * @param gridSize The grid cell size
     */
    private void parseObjectsSection(Element objectsElement, LevelGridView gridView, int gridSize) {
        // Parse player spawn
        NodeList playerNodes = objectsElement.getElementsByTagName("player");
        if (playerNodes.getLength() > 0) {
            parsePlayerSpawn((Element) playerNodes.item(0), gridView, gridSize);
        }

        // Parse blocks
        NodeList blocksNodes = objectsElement.getElementsByTagName("blocks");
        if (blocksNodes.getLength() > 0) {
            Element blocksElement = (Element) blocksNodes.item(0);
            parseBlocks(blocksElement, gridView, gridSize);
        }
        
        // Also parse individual block elements directly under objects (for decompressed levels)
        NodeList individualBlockNodes = objectsElement.getElementsByTagName("block");
        Log.d("DataDecoding", "🔄 LOAD: Found " + individualBlockNodes.getLength() + " individual block elements in LevelGridLoader");
        if (individualBlockNodes.getLength() > 0) {
            for (int i = 0; i < individualBlockNodes.getLength(); i++) {
                Element blockElement = (Element) individualBlockNodes.item(i);
                parseBlock(blockElement, gridView, gridSize);
            }
        }

        // Parse portals
        NodeList portalsNodes = objectsElement.getElementsByTagName("portals");
        if (portalsNodes.getLength() > 0) {
            Element portalsElement = (Element) portalsNodes.item(0);
            parsePortals(portalsElement, gridView, gridSize);
        }

        // Parse triggers
        NodeList triggersNodes = objectsElement.getElementsByTagName("triggers");
        if (triggersNodes.getLength() > 0) {
            Element triggersElement = (Element) triggersNodes.item(0);
            parseTriggers(triggersElement, gridView, gridSize);
        }
    }

    /**
     * Parses player spawn data and adds it to the grid.
     * 
     * @param playerElement The player element from XML
     * @param gridView The grid view to populate
     * @param gridSize The grid cell size
     */
    private void parsePlayerSpawn(Element playerElement, LevelGridView gridView, int gridSize) {
        try {
            // Get position
            NodeList positionNodes = playerElement.getElementsByTagName("position");
            if (positionNodes.getLength() > 0) {
                Element positionElement = (Element) positionNodes.item(0);
                double worldX = Double.parseDouble(positionElement.getAttribute("x"));
                double worldY = Double.parseDouble(positionElement.getAttribute("y"));

                // Convert world coordinates to grid coordinates
                int gridX = (int) (worldX / gridSize);
                int gridY = (int) (worldY / gridSize);

                // Create SimplePlayer with properties from XML
                SimplePlayer simplePlayer = createSimplePlayerFromXML(playerElement, worldX, worldY);

                // Add to grid
                gridView.addObjectToGrid(gridX, gridY, simplePlayer);

                Log.d(TAG, "Added player spawn at grid position: (" + gridX + ", " + gridY + ")");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing player spawn", e);
        }
    }

    /**
     * Creates a SimplePlayer from XML data.
     * 
     * @param playerElement The player element from XML
     * @param x The world X coordinate
     * @param y The world Y coordinate
     * @return The created SimplePlayer
     */
    private SimplePlayer createSimplePlayerFromXML(Element playerElement, double x, double y) {
        SimplePlayer simplePlayer = new SimplePlayer(x, y, 0); // Default groupId

        // Parse size
        NodeList sizeNodes = playerElement.getElementsByTagName("size");
        if (sizeNodes.getLength() > 0) {
            int size = Integer.parseInt(sizeNodes.item(0).getTextContent());
            simplePlayer.setSize(size);
        }

        // Parse health
        NodeList healthNodes = playerElement.getElementsByTagName("health");
        if (healthNodes.getLength() > 0) {
            int health = Integer.parseInt(healthNodes.item(0).getTextContent());
            simplePlayer.setHealth(health);
        }

        // Parse air
        NodeList airNodes = playerElement.getElementsByTagName("air");
        if (airNodes.getLength() > 0) {
            int air = Integer.parseInt(airNodes.item(0).getTextContent());
            simplePlayer.setAir(air);
        }

        // Parse physics properties
        NodeList physicsNodes = playerElement.getElementsByTagName("physics");
        if (physicsNodes.getLength() > 0) {
            Element physicsElement = (Element) physicsNodes.item(0);
            parsePhysicsProperties(simplePlayer, physicsElement);
        }

        return simplePlayer;
    }

    /**
     * Parses physics properties from XML and applies them to the SimplePlayer.
     * 
     * @param simplePlayer The SimplePlayer to modify
     * @param physicsElement The physics element from XML
     */
    private void parsePhysicsProperties(SimplePlayer simplePlayer, Element physicsElement) {
        // Parse speedX
        NodeList speedXNodes = physicsElement.getElementsByTagName("speedX");
        if (speedXNodes.getLength() > 0) {
            double speedX = Double.parseDouble(speedXNodes.item(0).getTextContent());
            simplePlayer.setSpeedX(speedX);
        }

        // Parse speedY
        NodeList speedYNodes = physicsElement.getElementsByTagName("speedY");
        if (speedYNodes.getLength() > 0) {
            double speedY = Double.parseDouble(speedYNodes.item(0).getTextContent());
            simplePlayer.setSpeedY(speedY);
        }

        // Parse gravity
        NodeList gravityNodes = physicsElement.getElementsByTagName("gravity");
        if (gravityNodes.getLength() > 0) {
            double gravity = Double.parseDouble(gravityNodes.item(0).getTextContent());
            simplePlayer.setGravity(gravity);
        }

        // Parse other physics properties
        parseGeneralPhysicsProperties(simplePlayer, physicsElement);
    }

    /**
     * Parses general physics properties (velocity, dimensions, etc.).
     * 
     * @param simpleObject The SimplePhysicsObject to modify
     * @param physicsElement The physics element from XML
     */
    private void parseGeneralPhysicsProperties(SimplePhysicsObject simpleObject, Element physicsElement) {
        // Parse velocity
        NodeList velXNodes = physicsElement.getElementsByTagName("velX");
        if (velXNodes.getLength() > 0) {
            double velX = Double.parseDouble(velXNodes.item(0).getTextContent());
            simpleObject.setVelX(velX);
        }

        NodeList velYNodes = physicsElement.getElementsByTagName("velY");
        if (velYNodes.getLength() > 0) {
            double velY = Double.parseDouble(velYNodes.item(0).getTextContent());
            simpleObject.setVelY(velY);
        }

        // Parse dimensions
        NodeList widthNodes = physicsElement.getElementsByTagName("width");
        if (widthNodes.getLength() > 0) {
            double width = Double.parseDouble(widthNodes.item(0).getTextContent());
            simpleObject.setWidth(width);
        }

        NodeList heightNodes = physicsElement.getElementsByTagName("height");
        if (heightNodes.getLength() > 0) {
            double height = Double.parseDouble(heightNodes.item(0).getTextContent());
            simpleObject.setHeight(height);
        }

        // Parse multipliers
        NodeList widthMultiplierNodes = physicsElement.getElementsByTagName("widthMultiplier");
        if (widthMultiplierNodes.getLength() > 0) {
            double widthMultiplier = Double.parseDouble(widthMultiplierNodes.item(0).getTextContent());
            simpleObject.setWidthMultiplier(widthMultiplier);
        }

        NodeList heightMultiplierNodes = physicsElement.getElementsByTagName("heightMultiplier");
        if (heightMultiplierNodes.getLength() > 0) {
            double heightMultiplier = Double.parseDouble(heightMultiplierNodes.item(0).getTextContent());
            simpleObject.setHeightMultiplier(heightMultiplier);
        }

        // Parse state
        NodeList activeNodes = physicsElement.getElementsByTagName("active");
        if (activeNodes.getLength() > 0) {
            boolean active = Boolean.parseBoolean(activeNodes.item(0).getTextContent());
            simpleObject.setActive(active);
        }

        NodeList visibleNodes = physicsElement.getElementsByTagName("visible");
        if (visibleNodes.getLength() > 0) {
            boolean visible = Boolean.parseBoolean(visibleNodes.item(0).getTextContent());
            simpleObject.setVisible(visible);
        }
    }

    /**
     * Parses blocks from XML and adds them to the grid.
     * 
     * @param blocksElement The blocks element from XML
     * @param gridView The grid view to populate
     * @param gridSize The grid cell size
     */
    private void parseBlocks(Element blocksElement, LevelGridView gridView, int gridSize) {
        NodeList blockNodes = blocksElement.getElementsByTagName("block");
        for (int i = 0; i < blockNodes.getLength(); i++) {
            Element blockElement = (Element) blockNodes.item(i);
            parseBlock(blockElement, gridView, gridSize);
        }
    }

    /**
     * Parses a single block from XML and adds it to the grid.
     * 
     * @param blockElement The block element from XML
     * @param gridView The grid view to populate
     * @param gridSize The grid cell size
     */
    private void parseBlock(Element blockElement, LevelGridView gridView, int gridSize) {
        try {
            String blockType = blockElement.getAttribute("type");
            
            // Get position
            NodeList positionNodes = blockElement.getElementsByTagName("position");
            if (positionNodes.getLength() > 0) {
                Element positionElement = (Element) positionNodes.item(0);
                double worldX = Double.parseDouble(positionElement.getAttribute("x"));
                double worldY = Double.parseDouble(positionElement.getAttribute("y"));

                // Convert world coordinates to grid coordinates
                int gridX = (int) (worldX / gridSize);
                int gridY = (int) (worldY / gridSize);

                // Get group ID
                int groupId = 0;
                NodeList groupIdNodes = blockElement.getElementsByTagName("groupId");
                if (groupIdNodes.getLength() > 0) {
                    groupId = Integer.parseInt(groupIdNodes.item(0).getTextContent());
                }

                // Create SimpleBlock based on type
                SimplePhysicsObject simpleBlock = createSimpleBlockFromType(blockType, worldX, worldY, groupId);

                // Parse physics properties
                NodeList physicsNodes = blockElement.getElementsByTagName("physics");
                if (physicsNodes.getLength() > 0) {
                    Element physicsElement = (Element) physicsNodes.item(0);
                    parseGeneralPhysicsProperties(simpleBlock, physicsElement);
                }

                // Add to grid
                gridView.addObjectToGrid(gridX, gridY, simpleBlock);

                Log.d(TAG, "Added " + blockType + " at grid position: (" + gridX + ", " + gridY + ")");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing block", e);
        }
    }

    /**
     * Creates a SimpleBlock based on the block type.
     * 
     * @param blockType The type of block to create
     * @param x The world X coordinate
     * @param y The world Y coordinate
     * @param groupId The group ID
     * @return The created SimpleBlock
     */
    private SimplePhysicsObject createSimpleBlockFromType(String blockType, double x, double y, int groupId) {
        switch (blockType) {
            case "SimpleBlockCommon":
                return new SimpleBlockCommon(x, y, groupId);
            case "SimpleBlockIce":
                return new SimpleBlockIce(x, y, groupId);
            case "SimpleBlockWater":
                return new SimpleBlockWater(x, y, groupId);
            case "SimpleBlockSpike":
            case "SimpleBlockSpikeCommon":
                return new SimpleBlockSpikeCommon(x, y, groupId);
            case "SimpleBlockSpikeCommonDown":
                return new SimpleBlockSpikeCommonDown(x, y, groupId);
            case "SimpleBlockSpikeCommonLeft":
                return new SimpleBlockSpikeCommonLeft(x, y, groupId);
            case "SimpleBlockSpikeCommonRight":
                return new SimpleBlockSpikeCommonRight(x, y, groupId);
            case "SimpleBlockSpikeIce":
                return new SimpleBlockSpikeIce(x, y, groupId);
            case "SimpleBlockSpikeIceDown":
                return new SimpleBlockSpikeIceDown(x, y, groupId);
            case "SimpleBlockSpikeIceLeft":
                return new SimpleBlockSpikeIceLeft(x, y, groupId);
            case "SimpleBlockSpikeIceRight":
                return new SimpleBlockSpikeIceRight(x, y, groupId);
            default:
                Log.w(TAG, "Unknown block type: " + blockType + ", using SimpleBlockCommon");
                return new SimpleBlockCommon(x, y, groupId);
        }
    }

    /**
     * Parses portals from XML and adds them to the grid.
     * 
     * @param portalsElement The portals element from XML
     * @param gridView The grid view to populate
     * @param gridSize The grid cell size
     */
    private void parsePortals(Element portalsElement, LevelGridView gridView, int gridSize) {
        NodeList portalNodes = portalsElement.getElementsByTagName("portal");
        for (int i = 0; i < portalNodes.getLength(); i++) {
            Element portalElement = (Element) portalNodes.item(i);
            parsePortal(portalElement, gridView, gridSize);
        }
    }

    /**
     * Parses a single portal from XML and adds it to the grid.
     * 
     * @param portalElement The portal element from XML
     * @param gridView The grid view to populate
     * @param gridSize The grid cell size
     */
    private void parsePortal(Element portalElement, LevelGridView gridView, int gridSize) {
        try {
            // Get position
            NodeList positionNodes = portalElement.getElementsByTagName("position");
            if (positionNodes.getLength() > 0) {
                Element positionElement = (Element) positionNodes.item(0);
                double worldX = Double.parseDouble(positionElement.getAttribute("x"));
                double worldY = Double.parseDouble(positionElement.getAttribute("y"));

                // Convert world coordinates to grid coordinates
                int gridX = (int) (worldX / gridSize);
                int gridY = (int) (worldY / gridSize);

                // Get group ID
                int groupId = 0;
                NodeList groupIdNodes = portalElement.getElementsByTagName("groupId");
                if (groupIdNodes.getLength() > 0) {
                    groupId = Integer.parseInt(groupIdNodes.item(0).getTextContent());
                }

                // Create SimplePortal
                SimplePhysicsObject simplePortal = new SimplePortal(worldX, worldY, groupId);

                // Parse physics properties
                NodeList physicsNodes = portalElement.getElementsByTagName("physics");
                if (physicsNodes.getLength() > 0) {
                    Element physicsElement = (Element) physicsNodes.item(0);
                    parseGeneralPhysicsProperties(simplePortal, physicsElement);
                }

                // Add to grid
                gridView.addObjectToGrid(gridX, gridY, simplePortal);

                Log.d(TAG, "Added portal at grid position: (" + gridX + ", " + gridY + ")");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing portal", e);
        }
    }

    /**
     * Parses triggers from XML and adds them to the grid.
     * 
     * @param triggersElement The triggers element from XML
     * @param gridView The grid view to populate
     * @param gridSize The grid cell size
     */
    private void parseTriggers(Element triggersElement, LevelGridView gridView, int gridSize) {
        NodeList triggerNodes = triggersElement.getElementsByTagName("trigger");
        for (int i = 0; i < triggerNodes.getLength(); i++) {
            Element triggerElement = (Element) triggerNodes.item(i);
            parseTrigger(triggerElement, gridView, gridSize);
        }
    }

    /**
     * Parses a single trigger from XML and adds it to the grid.
     * 
     * @param triggerElement The trigger element from XML
     * @param gridView The grid view to populate
     * @param gridSize The grid cell size
     */
    private void parseTrigger(Element triggerElement, LevelGridView gridView, int gridSize) {
        try {
            // Get position
            NodeList positionNodes = triggerElement.getElementsByTagName("position");
            if (positionNodes.getLength() > 0) {
                Element positionElement = (Element) positionNodes.item(0);
                double worldX = Double.parseDouble(positionElement.getAttribute("x"));
                double worldY = Double.parseDouble(positionElement.getAttribute("y"));

                // Convert world coordinates to grid coordinates
                int gridX = (int) (worldX / gridSize);
                int gridY = (int) (worldY / gridSize);

                // Get group ID
                int groupId = 0;
                NodeList groupIdNodes = triggerElement.getElementsByTagName("groupId");
                if (groupIdNodes.getLength() > 0) {
                    groupId = Integer.parseInt(groupIdNodes.item(0).getTextContent());
                }

                // Determine trigger type based on property sections
                String triggerType = determineTriggerType(triggerElement);

                // Create SimpleTrigger based on type
                SimplePhysicsObject simpleTrigger = createSimpleTriggerFromXML(triggerElement, worldX, worldY, groupId, triggerType);

                if (simpleTrigger != null) {
                    // Parse common trigger properties
                    parseCommonTriggerProperties(triggerElement, (SimpleTrigger) simpleTrigger);
                    
                    // Parse trigger-specific properties based on type
                    parseTriggerSpecificProperties(triggerElement, simpleTrigger, triggerType);
                    
                    // Add to grid
                    gridView.addObjectToGrid(gridX, gridY, simpleTrigger);
                    Log.d(TAG, "Added trigger " + triggerType + " at grid position: (" + gridX + ", " + gridY + ")");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing trigger", e);
        }
    }

    /**
     * Determines the trigger type based on the presence of specific property sections.
     * 
     * @param triggerElement The trigger element from XML
     * @return The trigger type string, or null if type cannot be determined
     */
    private String determineTriggerType(Element triggerElement) {
        // Check for specific property sections to determine trigger type
        if (triggerElement.getElementsByTagName("fade").getLength() > 0) {
            return "SimpleTriggerFade";
        } else if (triggerElement.getElementsByTagName("gravity").getLength() > 0) {
            return "SimpleTriggerGravity";
        } else if (triggerElement.getElementsByTagName("movement").getLength() > 0) {
            return "SimpleTriggerMovement";
        } else if (triggerElement.getElementsByTagName("scale").getLength() > 0) {
            return "SimpleTriggerScale";
        } else if (triggerElement.getElementsByTagName("translate").getLength() > 0) {
            return "SimpleTriggerTranslate";
        } else if (triggerElement.getElementsByTagName("inversion").getLength() > 0) {
            return "SimpleTriggerInversion";
        }
        
        // If no specific sections found, check if it's a basic trigger (inversion has no specific properties)
        // Inversion triggers don't have specific properties beyond the common ones
        return "SimpleTriggerInversion";
    }

    /**
     * Creates a SimpleTrigger from XML data based on the trigger type.
     * 
     * @param triggerElement The trigger element from XML
     * @param x The world X coordinate
     * @param y The world Y coordinate
     * @param groupId The group ID
     * @param triggerType The type of trigger
     * @return The created SimpleTrigger, or null if type is not supported
     */
    private SimplePhysicsObject createSimpleTriggerFromXML(Element triggerElement, double x, double y, int groupId, String triggerType) {
        switch (triggerType) {
            case "SimpleTriggerFade":
                return new SimpleTriggerFade((int)x, (int)y, groupId);
            case "SimpleTriggerGravity":
                return new SimpleTriggerGravity((int)x, (int)y, groupId);
            case "SimpleTriggerInversion":
                return new SimpleTriggerInversion((int)x, (int)y, groupId);
            case "SimpleTriggerMovement":
                return new SimpleTriggerMovement((int)x, (int)y, groupId);
            case "SimpleTriggerScale":
                return new SimpleTriggerScale((int)x, (int)y, groupId);
            case "SimpleTriggerTranslate":
                return new SimpleTriggerTranslate((int)x, (int)y, groupId);
            default:
                Log.w(TAG, "Unknown trigger type: " + triggerType);
                return null;
        }
    }

    /**
     * Parses common trigger properties from XML and applies them to the SimpleTrigger.
     * 
     * @param triggerElement The trigger element from XML
     * @param simpleTrigger The SimpleTrigger to modify
     */
    private void parseCommonTriggerProperties(Element triggerElement, SimpleTrigger simpleTrigger) {
        // Parse common trigger properties from the trigger sub-element
        NodeList triggerNodes = triggerElement.getElementsByTagName("trigger");
        if (triggerNodes.getLength() > 0) {
            Element triggerSubElement = (Element) triggerNodes.item(0);
            
            // Parse delay
            NodeList delayNodes = triggerSubElement.getElementsByTagName("delay");
            if (delayNodes.getLength() > 0) {
                int delay = Integer.parseInt(delayNodes.item(0).getTextContent());
                simpleTrigger.setDelay(delay);
            }
            
            // Parse speed
            NodeList speedNodes = triggerSubElement.getElementsByTagName("speed");
            if (speedNodes.getLength() > 0) {
                int speed = Integer.parseInt(speedNodes.item(0).getTextContent());
                simpleTrigger.setSpeed(speed);
            }
            
                    // targetGroupId removed - triggers use their own groupId as target
            
            // Parse bounds
            NodeList wr1Nodes = triggerSubElement.getElementsByTagName("WR1");
            if (wr1Nodes.getLength() > 0) {
                int wr1 = Integer.parseInt(wr1Nodes.item(0).getTextContent());
                simpleTrigger.setWR1(wr1);
            }
            
            NodeList wr2Nodes = triggerSubElement.getElementsByTagName("WR2");
            if (wr2Nodes.getLength() > 0) {
                int wr2 = Integer.parseInt(wr2Nodes.item(0).getTextContent());
                simpleTrigger.setWR2(wr2);
            }
            
            NodeList hr1Nodes = triggerSubElement.getElementsByTagName("HR1");
            if (hr1Nodes.getLength() > 0) {
                int hr1 = Integer.parseInt(hr1Nodes.item(0).getTextContent());
                simpleTrigger.setHR1(hr1);
            }
            
            NodeList hr2Nodes = triggerSubElement.getElementsByTagName("HR2");
            if (hr2Nodes.getLength() > 0) {
                int hr2 = Integer.parseInt(hr2Nodes.item(0).getTextContent());
                simpleTrigger.setHR2(hr2);
            }
        }
    }

    /**
     * Parses trigger-specific properties from XML based on the trigger type.
     * 
     * @param triggerElement The trigger element from XML
     * @param simpleTrigger The SimpleTrigger to modify
     * @param triggerType The type of trigger
     */
    private void parseTriggerSpecificProperties(Element triggerElement, SimplePhysicsObject simpleTrigger, String triggerType) {
        switch (triggerType) {
            case "SimpleTriggerFade":
                parseFadeTriggerProperties(triggerElement, (SimpleTriggerFade) simpleTrigger);
                break;
            case "SimpleTriggerGravity":
                parseGravityTriggerProperties(triggerElement, (SimpleTriggerGravity) simpleTrigger);
                break;
            case "SimpleTriggerMovement":
                parseMovementTriggerProperties(triggerElement, (SimpleTriggerMovement) simpleTrigger);
                break;
            case "SimpleTriggerScale":
                parseScaleTriggerProperties(triggerElement, (SimpleTriggerScale) simpleTrigger);
                break;
            case "SimpleTriggerTranslate":
                parseTranslateTriggerProperties(triggerElement, (SimpleTriggerTranslate) simpleTrigger);
                break;
        }
    }

    private void parseFadeTriggerProperties(Element triggerElement, SimpleTriggerFade trigger) {
        NodeList fadeNodes = triggerElement.getElementsByTagName("fade");
        if (fadeNodes.getLength() > 0) {
            Element fadeElement = (Element) fadeNodes.item(0);
            
            NodeList targetOpacityNodes = fadeElement.getElementsByTagName("targetOpacity");
            if (targetOpacityNodes.getLength() > 0) {
                double targetOpacity = Double.parseDouble(targetOpacityNodes.item(0).getTextContent());
                trigger.setTargetOpacity(targetOpacity);
            }
            
            NodeList removeCollisionNodes = fadeElement.getElementsByTagName("removeCollisionAtZero");
            if (removeCollisionNodes.getLength() > 0) {
                boolean removeCollision = Boolean.parseBoolean(removeCollisionNodes.item(0).getTextContent());
                trigger.setRemoveCollisionAtZero(removeCollision);
            }
        }
    }

    private void parseGravityTriggerProperties(Element triggerElement, SimpleTriggerGravity trigger) {
        NodeList gravityNodes = triggerElement.getElementsByTagName("gravity");
        if (gravityNodes.getLength() > 0) {
            Element gravityElement = (Element) gravityNodes.item(0);
            
            NodeList strengthNodes = gravityElement.getElementsByTagName("strength");
            if (strengthNodes.getLength() > 0) {
                double strength = Double.parseDouble(strengthNodes.item(0).getTextContent());
                trigger.setGravityStrength(strength);
            }
            
            NodeList directionNodes = gravityElement.getElementsByTagName("direction");
            if (directionNodes.getLength() > 0) {
                String directionStr = directionNodes.item(0).getTextContent();
                SimpleTriggerGravity.GravityDirection direction = 
                    SimpleTriggerGravity.GravityDirection.valueOf(directionStr);
                trigger.setGravityDirection(direction);
            }
        }
    }

    private void parseMovementTriggerProperties(Element triggerElement, SimpleTriggerMovement trigger) {
        NodeList movementNodes = triggerElement.getElementsByTagName("movement");
        if (movementNodes.getLength() > 0) {
            Element movementElement = (Element) movementNodes.item(0);
            
            NodeList xSpeedNodes = movementElement.getElementsByTagName("xSpeed");
            if (xSpeedNodes.getLength() > 0) {
                double xSpeed = Double.parseDouble(xSpeedNodes.item(0).getTextContent());
                trigger.setXSpeed(xSpeed);
            }
            
            NodeList ySpeedNodes = movementElement.getElementsByTagName("ySpeed");
            if (ySpeedNodes.getLength() > 0) {
                double ySpeed = Double.parseDouble(ySpeedNodes.item(0).getTextContent());
                trigger.setYSpeed(ySpeed);
            }
            
            NodeList gravityIntensityNodes = movementElement.getElementsByTagName("gravityIntensity");
            if (gravityIntensityNodes.getLength() > 0) {
                double gravityIntensity = Double.parseDouble(gravityIntensityNodes.item(0).getTextContent());
                trigger.setGravityIntensity(gravityIntensity);
            }
        }
    }

    private void parseScaleTriggerProperties(Element triggerElement, SimpleTriggerScale trigger) {
        NodeList scaleNodes = triggerElement.getElementsByTagName("scale");
        if (scaleNodes.getLength() > 0) {
            Element scaleElement = (Element) scaleNodes.item(0);
            
            NodeList targetHeightNodes = scaleElement.getElementsByTagName("targetHeight");
            if (targetHeightNodes.getLength() > 0) {
                double targetHeight = Double.parseDouble(targetHeightNodes.item(0).getTextContent());
                trigger.setTargetHeight(targetHeight);
            }
            
            NodeList targetWidthNodes = scaleElement.getElementsByTagName("targetWidth");
            if (targetWidthNodes.getLength() > 0) {
                double targetWidth = Double.parseDouble(targetWidthNodes.item(0).getTextContent());
                trigger.setTargetWidth(targetWidth);
            }
        }
    }

    private void parseTranslateTriggerProperties(Element triggerElement, SimpleTriggerTranslate trigger) {
        NodeList translateNodes = triggerElement.getElementsByTagName("translate");
        if (translateNodes.getLength() > 0) {
            Element translateElement = (Element) translateNodes.item(0);
            
            NodeList directionNodes = translateElement.getElementsByTagName("direction");
            if (directionNodes.getLength() > 0) {
                String directionStr = directionNodes.item(0).getTextContent();
                SimpleTriggerTranslate.Direction direction = 
                    SimpleTriggerTranslate.Direction.valueOf(directionStr);
                trigger.setDirection(direction);
            }
            
            NodeList blockCountNodes = translateElement.getElementsByTagName("blockCount");
            if (blockCountNodes.getLength() > 0) {
                int blockCount = Integer.parseInt(blockCountNodes.item(0).getTextContent());
                trigger.setBlockCount(blockCount);
            }
        }
    }
} 
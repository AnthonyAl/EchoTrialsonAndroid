package com.unipi.alexandris.android.echotrialsonandroid.controller;

import android.content.Context;
import android.util.Log;

import com.unipi.alexandris.android.echotrialsonandroid.data.simpleModel.*;
import com.unipi.alexandris.android.echotrialsonandroid.utility.LevelID;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * DataEncoder class for handling XML encoding/decoding of level data.
 * 
 * <h3>Save Categories:</h3>
 * <ol>
 *   <li><b>Base Model:</b> Blocks, Players, basic physics objects (CURRENT)</li>
 *   <li><b>SimpleTrigger Blocks:</b> Interactive triggers and their properties (FUTURE)</li>
 *   <li><b>SimplePlayer Properties:</b> SimplePlayer state, health, position, etc. (FUTURE)</li>
 * </ol>
 * 
 * <h3>XML Structure:</h3>
 * <pre>
 * &lt;level&gt;
 *   &lt;metadata&gt;
 *     &lt;levelId&gt;LEVEL_A_I&lt;/levelId&gt;
 *     &lt;version&gt;1.0&lt;/version&gt;
 *     &lt;saveDate&gt;2024-01-01T12:00:00&lt;/saveDate&gt;
 *     &lt;gridSize&gt;32&lt;/gridSize&gt;
 *   &lt;/metadata&gt;
 *   &lt;objects&gt;
 *     &lt;player&gt;
 *       &lt;position x="100.0" y="200.0" /&gt;
 *       &lt;size&gt;48&lt;/size&gt;
 *       &lt;health&gt;3&lt;/health&gt;
 *       &lt;air&gt;100&lt;/air&gt;
 *       &lt;physics&gt;
 *         &lt;speedX&gt;15.0&lt;/speedX&gt;
 *         &lt;speedY&gt;20.0&lt;/speedY&gt;
 *         &lt;gravity&gt;0.9&lt;/gravity&gt;
 *         &lt;velX&gt;0.0&lt;/velX&gt;
 *         &lt;velY&gt;0.0&lt;/velY&gt;
 *         &lt;width&gt;48.0&lt;/width&gt;
 *         &lt;height&gt;62.4&lt;/height&gt;
 *         &lt;widthMultiplier&gt;1.0&lt;/widthMultiplier&gt;
 *         &lt;heightMultiplier&gt;1.3&lt;/heightMultiplier&gt;
 *         &lt;active&gt;true&lt;/active&gt;
 *         &lt;visible&gt;true&lt;/visible&gt;
 *       &lt;/physics&gt;
 *       &lt;state&gt;
 *         &lt;inAir&gt;false&lt;/inAir&gt;
 *         &lt;onIce&gt;false&lt;/onIce&gt;
 *         &lt;inWater&gt;false&lt;/inWater&gt;
 *         &lt;death&gt;false&lt;/death&gt;
 *       &lt;/state&gt;
 *     &lt;/player&gt;
 *     &lt;blocks&gt;
 *       &lt;block type="SimpleBlockCommon"&gt;
 *         &lt;position x="0.0" y="400.0" /&gt;
 *         &lt;size&gt;48&lt;/size&gt;
 *         &lt;groupId&gt;0&lt;/groupId&gt;
 *         &lt;physics&gt;
 *           &lt;velX&gt;0.0&lt;/velX&gt;
 *           &lt;velY&gt;0.0&lt;/velY&gt;
 *           &lt;width&gt;48.0&lt;/width&gt;
 *           &lt;height&gt;48.0&lt;/height&gt;
 *           &lt;widthMultiplier&gt;1.0&lt;/widthMultiplier&gt;
 *           &lt;heightMultiplier&gt;1.0&lt;/heightMultiplier&gt;
 *           &lt;active&gt;true&lt;/active&gt;
 *           &lt;visible&gt;true&lt;/visible&gt;
 *         &lt;/physics&gt;
 *         &lt;region&gt;
 *           &lt;bounds left="0" top="400" right="48" bottom="448" /&gt;
 *         &lt;/region&gt;
 *       &lt;/block&gt;
 *       &lt;block type="SimpleBlockIce"&gt;
 *         &lt;position x="100.0" y="400.0" /&gt;
 *         &lt;size&gt;48&lt;/size&gt;
 *         &lt;groupId&gt;1&lt;/groupId&gt;
 *         &lt;physics&gt;
 *           &lt;velX&gt;0.0&lt;/velX&gt;
 *           &lt;velY&gt;0.0&lt;/velY&gt;
 *           &lt;width&gt;48.0&lt;/width&gt;
 *           &lt;height&gt;48.0&lt;/height&gt;
 *           &lt;widthMultiplier&gt;1.0&lt;/widthMultiplier&gt;
 *           &lt;heightMultiplier&gt;1.0&lt;/heightMultiplier&gt;
 *           &lt;active&gt;true&lt;/active&gt;
 *           &lt;visible&gt;true&lt;/visible&gt;
 *         &lt;/physics&gt;
 *         &lt;region&gt;
 *           &lt;bounds left="100" top="400" right="148" bottom="448" /&gt;
 *         &lt;/region&gt;
 *       &lt;/block&gt;
 *     &lt;/blocks&gt;
 *     &lt;portals&gt;
 *       &lt;portal&gt;
 *         &lt;position x="500.0" y="300.0" /&gt;
 *         &lt;size&gt;48&lt;/size&gt;
 *         &lt;groupId&gt;0&lt;/groupId&gt;
 *         &lt;physics&gt;
 *           &lt;velX&gt;0.0&lt;/velX&gt;
 *           &lt;velY&gt;0.0&lt;/velY&gt;
 *           &lt;width&gt;48.0&lt;/width&gt;
 *           &lt;height&gt;48.0&lt;/height&gt;
 *           &lt;widthMultiplier&gt;1.0&lt;/widthMultiplier&gt;
 *           &lt;heightMultiplier&gt;1.0&lt;/heightMultiplier&gt;
 *           &lt;active&gt;true&lt;/active&gt;
 *           &lt;visible&gt;true&lt;/visible&gt;
 *         &lt;/physics&gt;
 *       &lt;/portal&gt;
 *     &lt;/portals&gt;
 *   &lt;/objects&gt;
 * &lt;/level&gt;
 * </pre>
 */
public class DataEncoder {
    private static final String TAG = "DataEncoder";
    private final Context context;
    private final DataCompressionHelper compressionHelper;
    
    /**
     * Creates a new DataEncoder instance with the specified context.
     * 
     * @param context The Android context for file operations
     */
    public DataEncoder(Context context) {
        this.context = context;
        this.compressionHelper = new DataCompressionHelper();
    }

    /**
     * LEGACY METHOD: Encodes and saves locally, instead of uploading to the Cloud.
     * <br/>
     * Encodes level data to XML using SimpleModel objects with custom level name.
     * This method provides full property access and saves with a custom filename.
     *
     * @param levelID The level ID for metadata
     * @param simpleGridData Map of grid coordinates to SimplePhysicsObject instances
     * @param gridSize The size of each grid cell
     * @param playerSpawnX The player spawn X coordinate in grid units
     * @param playerSpawnY The player spawn Y coordinate in grid units
     * @param customLevelName Custom name for the level (can be null for default)
     * @return true if encoding was successful
     */
    @SuppressWarnings("unused")
    public boolean encodeSimpleLevelToXML(LevelID levelID, Map<String, SimplePhysicsObject> simpleGridData,
                                        int gridSize, int playerSpawnX, int playerSpawnY, String customLevelName) {
        if (simpleGridData != null && simpleGridData.size() > 10000) { // Using hardcoded value since we can't import LevelGridView here
            Log.e(TAG, "Cannot encode level - object count (" + simpleGridData.size() + ") exceeds maximum limit of 10,000");
            return false;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element rootElement = document.createElement("level");
            document.appendChild(rootElement);

            addMetadata(document, rootElement, levelID, gridSize);

            addObjectsFromSimpleGrid(document, rootElement, simpleGridData, gridSize, playerSpawnX, playerSpawnY);

            return saveDocumentToFile(document, levelID, customLevelName);

        } catch (Exception e) {
            Log.e(TAG, "Error encoding level to XML: " + e.getMessage());
            return false;
        }
    }

    private void addMetadata(Document document, Element rootElement, LevelID levelID, int gridSize) {
        Element metadataElement = document.createElement("metadata");
        rootElement.appendChild(metadataElement);
        
        // Level SimpleID
        Element levelIdElement = document.createElement("levelId");
        levelIdElement.setTextContent(levelID.name());
        metadataElement.appendChild(levelIdElement);
        
        // Version
        Element versionElement = document.createElement("version");
        versionElement.setTextContent("1.0");
        metadataElement.appendChild(versionElement);
        
        // Save date
        Element saveDateElement = document.createElement("saveDate");
        saveDateElement.setTextContent(java.time.LocalDateTime.now().toString());
        metadataElement.appendChild(saveDateElement);
        
        // Grid size
        Element gridSizeElement = document.createElement("gridSize");
        gridSizeElement.setTextContent(String.valueOf(gridSize));
        metadataElement.appendChild(gridSizeElement);
    }

    private void addObjectsFromSimpleGrid(Document document, Element rootElement, 
                                        Map<String, SimplePhysicsObject> simpleGridData, int gridSize,
                                        int playerSpawnX, int playerSpawnY) {
        Element objectsElement = document.createElement("objects");
        rootElement.appendChild(objectsElement);

        addGridObjects(document, objectsElement, simpleGridData, playerSpawnX, playerSpawnY, gridSize);
    }

    private void addPlayerSpawn(Document document, Element objectsElement, 
                               SimplePlayer simplePlayer) {
        Element playerElement = document.createElement("player");
        objectsElement.appendChild(playerElement);
        
        // Position (convert grid coordinates to world coordinates)
        Element positionElement = document.createElement("position");
        positionElement.setAttribute("x", String.valueOf(simplePlayer.getX()));
        positionElement.setAttribute("y", String.valueOf(simplePlayer.getY()));
        playerElement.appendChild(positionElement);
        
        // Use SimpleModel values
        Element sizeElement = document.createElement("size");
        sizeElement.setTextContent(String.valueOf(simplePlayer.getSize()));
        playerElement.appendChild(sizeElement);
        
        Element healthElement = document.createElement("health");
        healthElement.setTextContent(String.valueOf(simplePlayer.getHealth()));
        playerElement.appendChild(healthElement);
        
        Element airElement = document.createElement("air");
        airElement.setTextContent(String.valueOf(simplePlayer.getAir()));
        playerElement.appendChild(airElement);
        
        // Group ID
        Element groupIdElement = document.createElement("groupId");
        groupIdElement.setTextContent(String.valueOf(simplePlayer.getGroupId()));
        playerElement.appendChild(groupIdElement);
        
        // Physics properties from SimpleModel
        Element physicsElement = document.createElement("physics");
        playerElement.appendChild(physicsElement);
        
        Element speedXElement = document.createElement("speedX");
        speedXElement.setTextContent(String.valueOf(simplePlayer.getSpeedX()));
        physicsElement.appendChild(speedXElement);
        
        Element speedYElement = document.createElement("speedY");
        speedYElement.setTextContent(String.valueOf(simplePlayer.getSpeedY()));
        physicsElement.appendChild(speedYElement);
        
        Element gravityElement = document.createElement("gravity");
        gravityElement.setTextContent(String.valueOf(simplePlayer.getGravity()));
        physicsElement.appendChild(gravityElement);

        // Velocity
        Element velXElement = document.createElement("velX");
        velXElement.setTextContent(String.valueOf(simplePlayer.getVelX()));
        physicsElement.appendChild(velXElement);

        Element velYElement = document.createElement("velY");
        velYElement.setTextContent(String.valueOf(simplePlayer.getVelY()));
        physicsElement.appendChild(velYElement);

        // Dimensions
        Element widthElement = document.createElement("width");
        widthElement.setTextContent(String.valueOf(simplePlayer.getWidth()));
        physicsElement.appendChild(widthElement);

        Element heightElement = document.createElement("height");
        heightElement.setTextContent(String.valueOf(simplePlayer.getHeight()));
        physicsElement.appendChild(heightElement);

        // Multipliers
        Element widthMultiplierElement = document.createElement("widthMultiplier");
        widthMultiplierElement.setTextContent(String.valueOf(simplePlayer.getWidthMultiplier()));
        physicsElement.appendChild(widthMultiplierElement);

        Element heightMultiplierElement = document.createElement("heightMultiplier");
        heightMultiplierElement.setTextContent(String.valueOf(simplePlayer.getHeightMultiplier()));
        physicsElement.appendChild(heightMultiplierElement);

        // State
        Element activeElement = document.createElement("active");
        activeElement.setTextContent(String.valueOf(simplePlayer.isActive()));
        physicsElement.appendChild(activeElement);

        Element visibleElement = document.createElement("visible");
        visibleElement.setTextContent(String.valueOf(simplePlayer.isVisible()));
        physicsElement.appendChild(visibleElement);
        
        // Default state from SimpleModel
        Element stateElement = document.createElement("state");
        playerElement.appendChild(stateElement);
        
        Element inAirElement = document.createElement("inAir");
        inAirElement.setTextContent(String.valueOf(simplePlayer.isInAir()));
        stateElement.appendChild(inAirElement);
        
        Element onIceElement = document.createElement("onIce");
        onIceElement.setTextContent(String.valueOf(simplePlayer.isOnIce()));
        stateElement.appendChild(onIceElement);
        
        Element inWaterElement = document.createElement("inWater");
        inWaterElement.setTextContent(String.valueOf(simplePlayer.isInWater()));
        stateElement.appendChild(inWaterElement);
        
        Element deathElement = document.createElement("death");
        deathElement.setTextContent("false");
        stateElement.appendChild(deathElement);
    }

    private void addGridObjects(Document document, Element objectsElement, 
                               Map<String, SimplePhysicsObject> simpleGridData,
                                int playerSpawnX, int playerSpawnY, int gridSize) {
        
        for (Map.Entry<String, SimplePhysicsObject> entry : simpleGridData.entrySet()) {
            String gridKey = entry.getKey();
            SimplePhysicsObject simpleObject = entry.getValue();

            if (simpleObject.getId().name().equals("Player")) {
                SimplePlayer player = (SimplePlayer) simpleObject;
                player.setX(playerSpawnX * gridSize);
                player.setY(playerSpawnY * gridSize);
                addPlayerSpawn(document, objectsElement, player);
                continue;
            }

            String[] keyParts = gridKey.split(":");
            if (keyParts.length == 2) {
                String[] coords = keyParts[0].split(",");
                int gridX = Integer.parseInt(coords[0]);
                int gridY = Integer.parseInt(coords[1]);
                int groupId = Integer.parseInt(keyParts[1]);

                simpleObject.setX(gridX * gridSize);
                simpleObject.setY(gridY * gridSize);
                simpleObject.setGroupId(groupId);
            } else {
                String[] coords = gridKey.split(",");
                int gridX = Integer.parseInt(coords[0]);
                int gridY = Integer.parseInt(coords[1]);

                simpleObject.setX(gridX * gridSize);
                simpleObject.setY(gridY * gridSize);
            }

            String gameObjectType = getGameObjectTypeFromSimpleID(simpleObject.getId());
            if (gameObjectType == null) {
                Log.w("DataDecoding", "SAVE: Unknown SimplePhysicsObject type: " + simpleObject.getId());
                continue;
            }

            Element sectionElement = getOrCreateSection(document, objectsElement, gameObjectType);
            addSimpleObjectToSection(document, sectionElement, simpleObject, gridSize);
        }
    }

    private String getGameObjectTypeFromSimpleID(SimpleID simpleID) {
        switch (simpleID) {
            case BlockCommon:
                return "SimpleBlockCommon";
            case BlockIce:
                return "SimpleBlockIce";
            case BlockWater:
                return "SimpleBlockWater";
            case BlockSpikeCommon:
                return "SimpleBlockSpikeCommon";
            case BlockSpikeCommonDown:
                return "SimpleBlockSpikeCommonDown";
            case BlockSpikeCommonLeft:
                return "SimpleBlockSpikeCommonLeft";
            case BlockSpikeCommonRight:
                return "SimpleBlockSpikeCommonRight";
            case BlockSpikeIce:
                return "SimpleBlockSpikeIce";
            case BlockSpikeIceDown:
                return "SimpleBlockSpikeIceDown";
            case BlockSpikeIceLeft:
                return "SimpleBlockSpikeIceLeft";
            case BlockSpikeIceRight:
                return "SimpleBlockSpikeIceRight";
            case Portal:
                return "SimplePortal";
            case Player:
                return "SimplePlayer";
            case TriggerFade:
                return "SimpleTriggerFade";
            case TriggerGravity:
                return "SimpleTriggerGravity";
            case TriggerInversion:
                return "SimpleTriggerInversion";
            case TriggerMovement:
                return "SimpleTriggerMovement";
            case TriggerScale:
                return "SimpleTriggerScale";
            case TriggerTranslate:
                return "SimpleTriggerTranslate";
            default:
                return null;
        }
    }

    private void addSimpleObjectToSection(Document document, Element sectionElement, 
                                        SimplePhysicsObject simpleObject, int gridSize) {
        String elementName = getElementNameFromSimpleID(simpleObject.getId());
        Element objectElement = document.createElement(elementName);
        
        // Set type attribute for blocks
        if (simpleObject.getId().name().startsWith("Block")) {
            String typeString = getGameObjectTypeFromSimpleID(simpleObject.getId());
            objectElement.setAttribute("type", typeString);
        }
        
        // Position
        Element positionElement = document.createElement("position");
        positionElement.setAttribute("x", String.valueOf(simpleObject.getX()));
        positionElement.setAttribute("y", String.valueOf(simpleObject.getY()));
        objectElement.appendChild(positionElement);
        
        // Size
        Element sizeElement = document.createElement("size");
        sizeElement.setTextContent(String.valueOf(simpleObject.getSize()));
        objectElement.appendChild(sizeElement);
        
        // Group ID
        Element groupIdElement = document.createElement("groupId");
        groupIdElement.setTextContent(String.valueOf(simpleObject.getGroupId()));
        objectElement.appendChild(groupIdElement);
        
        // Add physics properties for all objects
        addPhysicsProperties(document, objectElement, simpleObject);
        
        // Add region bounds for blocks
        if (simpleObject.getId().name().startsWith("Block")) {
            addRegionBounds(document, objectElement, simpleObject.getX(), simpleObject.getY(), gridSize);
        }
        
        // Add trigger-specific properties
        if (simpleObject.getId().name().startsWith("Trigger")) {
            addTriggerProperties(document, objectElement, simpleObject);
        }
        
        sectionElement.appendChild(objectElement);
    }

    private void addPhysicsProperties(Document document, Element objectElement, SimplePhysicsObject simpleObject) {
        Element physicsElement = document.createElement("physics");
        objectElement.appendChild(physicsElement);
        
        // Velocity
        Element velXElement = document.createElement("velX");
        velXElement.setTextContent(String.valueOf(simpleObject.getVelX()));
        physicsElement.appendChild(velXElement);
        
        Element velYElement = document.createElement("velY");
        velYElement.setTextContent(String.valueOf(simpleObject.getVelY()));
        physicsElement.appendChild(velYElement);
        
        // Dimensions
        Element widthElement = document.createElement("width");
        widthElement.setTextContent(String.valueOf(simpleObject.getWidth()));
        physicsElement.appendChild(widthElement);
        
        Element heightElement = document.createElement("height");
        heightElement.setTextContent(String.valueOf(simpleObject.getHeight()));
        physicsElement.appendChild(heightElement);
        
        // Multipliers
        Element widthMultiplierElement = document.createElement("widthMultiplier");
        widthMultiplierElement.setTextContent(String.valueOf(simpleObject.getWidthMultiplier()));
        physicsElement.appendChild(widthMultiplierElement);
        
        Element heightMultiplierElement = document.createElement("heightMultiplier");
        heightMultiplierElement.setTextContent(String.valueOf(simpleObject.getHeightMultiplier()));
        physicsElement.appendChild(heightMultiplierElement);
        
        // State
        Element activeElement = document.createElement("active");
        activeElement.setTextContent(String.valueOf(simpleObject.isActive()));
        physicsElement.appendChild(activeElement);
        
        Element visibleElement = document.createElement("visible");
        visibleElement.setTextContent(String.valueOf(simpleObject.isVisible()));
        physicsElement.appendChild(visibleElement);
    }

    private String getSectionName(String objectType) {
        if (objectType.startsWith("SimpleBlock")) {
            return "blocks";
        } else if (objectType.equals("SimplePortal")) {
            return "portals";
        } else if (objectType.startsWith("SimpleTrigger")) {
            return "triggers";
        } else {
            return objectType.toLowerCase() + "s";
        }
    }

    private String getElementNameFromSimpleID(SimpleID simpleID) {
        String name = simpleID.name();
        if (name.startsWith("Block")) {
            return "block";
        } else if (name.equals("Portal")) {
            return "portal";
        } else if (name.startsWith("Trigger")) {
            return "trigger";
        } else {
            return name.toLowerCase();
        }
    }

    private Element getOrCreateSection(Document document, Element objectsElement, String objectType) {
        String sectionName = getSectionName(objectType);
        NodeList existingSections = objectsElement.getElementsByTagName(sectionName);
        
        if (existingSections.getLength() > 0) {
            return (Element) existingSections.item(0);
        } else {
            Element sectionElement = document.createElement(sectionName);
            objectsElement.appendChild(sectionElement);
            return sectionElement;
        }
    }

    private void addTriggerProperties(Document document, Element objectElement, SimplePhysicsObject simpleObject) {
        if (!(simpleObject instanceof SimpleTrigger)) {
            return;
        }
        
        SimpleTrigger trigger = 
            (SimpleTrigger) simpleObject;
        addCommonTriggerProperties(document, objectElement, trigger);
        String triggerType = simpleObject.getId().name();
        
        switch (triggerType) {
            case "TriggerFade":
                addFadeTriggerProperties(document, objectElement, 
                    (SimpleTriggerFade) trigger);
                break;
            case "TriggerGravity":
                addGravityTriggerProperties(document, objectElement, 
                    (SimpleTriggerGravity) trigger);
                break;
            case "TriggerMovement":
                addMovementTriggerProperties(document, objectElement, 
                    (SimpleTriggerMovement) trigger);
                break;
            case "TriggerScale":
                addScaleTriggerProperties(document, objectElement, 
                    (SimpleTriggerScale) trigger);
                break;
            case "TriggerTranslate":
                addTranslateTriggerProperties(document, objectElement, 
                    (SimpleTriggerTranslate) trigger);
                break;
        }
    }

    private void addCommonTriggerProperties(Document document, Element objectElement, 
                                          SimpleTrigger trigger) {
        Element triggerElement = document.createElement("trigger");
        
        // Common trigger properties
        Element delayElement = document.createElement("delay");
        delayElement.setTextContent(String.valueOf(trigger.getDelay()));
        triggerElement.appendChild(delayElement);
        
        Element speedElement = document.createElement("speed");
        speedElement.setTextContent(String.valueOf(trigger.getSpeed()));
        triggerElement.appendChild(speedElement);
        
        // Bounds
        Element wr1Element = document.createElement("WR1");
        wr1Element.setTextContent(String.valueOf(trigger.getWR1()));
        triggerElement.appendChild(wr1Element);
        
        Element wr2Element = document.createElement("WR2");
        wr2Element.setTextContent(String.valueOf(trigger.getWR2()));
        triggerElement.appendChild(wr2Element);
        
        Element hr1Element = document.createElement("HR1");
        hr1Element.setTextContent(String.valueOf(trigger.getHR1()));
        triggerElement.appendChild(hr1Element);
        
        Element hr2Element = document.createElement("HR2");
        hr2Element.setTextContent(String.valueOf(trigger.getHR2()));
        triggerElement.appendChild(hr2Element);
        
        objectElement.appendChild(triggerElement);
    }

    private void addFadeTriggerProperties(Document document, Element objectElement, 
                                        SimpleTriggerFade trigger) {
        Element fadeElement = document.createElement("fade");
        
        Element targetOpacityElement = document.createElement("targetOpacity");
        targetOpacityElement.setTextContent(String.valueOf(trigger.getTargetOpacity()));
        fadeElement.appendChild(targetOpacityElement);
        
        Element removeCollisionElement = document.createElement("removeCollisionAtZero");
        removeCollisionElement.setTextContent(String.valueOf(trigger.isRemoveCollisionAtZero()));
        fadeElement.appendChild(removeCollisionElement);
        
        objectElement.appendChild(fadeElement);
    }

    private void addGravityTriggerProperties(Document document, Element objectElement, 
                                           SimpleTriggerGravity trigger) {
        Element gravityElement = document.createElement("gravity");
        
        Element strengthElement = document.createElement("strength");
        strengthElement.setTextContent(String.valueOf(trigger.getGravityStrength()));
        gravityElement.appendChild(strengthElement);
        
        Element directionElement = document.createElement("direction");
        directionElement.setTextContent(trigger.getGravityDirection().name());
        gravityElement.appendChild(directionElement);
        
        objectElement.appendChild(gravityElement);
    }

    private void addMovementTriggerProperties(Document document, Element objectElement, 
                                            SimpleTriggerMovement trigger) {
        Element movementElement = document.createElement("movement");
        
        Element xSpeedElement = document.createElement("xSpeed");
        xSpeedElement.setTextContent(String.valueOf(trigger.getXSpeed()));
        movementElement.appendChild(xSpeedElement);
        
        Element ySpeedElement = document.createElement("ySpeed");
        ySpeedElement.setTextContent(String.valueOf(trigger.getYSpeed()));
        movementElement.appendChild(ySpeedElement);
        
        Element gravityIntensityElement = document.createElement("gravityIntensity");
        gravityIntensityElement.setTextContent(String.valueOf(trigger.getGravityIntensity()));
        movementElement.appendChild(gravityIntensityElement);
        
        objectElement.appendChild(movementElement);
    }

    private void addScaleTriggerProperties(Document document, Element objectElement, 
                                         SimpleTriggerScale trigger) {
        Element scaleElement = document.createElement("scale");
        
        Element targetHeightElement = document.createElement("targetHeight");
        targetHeightElement.setTextContent(String.valueOf(trigger.getTargetHeight()));
        scaleElement.appendChild(targetHeightElement);
        
        Element targetWidthElement = document.createElement("targetWidth");
        targetWidthElement.setTextContent(String.valueOf(trigger.getTargetWidth()));
        scaleElement.appendChild(targetWidthElement);
        
        objectElement.appendChild(scaleElement);
    }

    private void addTranslateTriggerProperties(Document document, Element objectElement, 
                                             SimpleTriggerTranslate trigger) {
        Element translateElement = document.createElement("translate");
        
        Element directionElement = document.createElement("direction");
        directionElement.setTextContent(trigger.getDirection().name());
        translateElement.appendChild(directionElement);
        
        Element blockCountElement = document.createElement("blockCount");
        blockCountElement.setTextContent(String.valueOf(trigger.getBlockCount()));
        translateElement.appendChild(blockCountElement);
        
        objectElement.appendChild(translateElement);
    }

    private void addRegionBounds(Document document, Element objectElement, 
                                double x, double y, int gridSize) {
        Element regionElement = document.createElement("region");
        objectElement.appendChild(regionElement);
        
        Element boundsElement = document.createElement("bounds");
        boundsElement.setAttribute("left", String.valueOf((int)x));
        boundsElement.setAttribute("top", String.valueOf((int)y));
        boundsElement.setAttribute("right", String.valueOf((int)(x + gridSize)));
        boundsElement.setAttribute("bottom", String.valueOf((int)(y + gridSize)));
        regionElement.appendChild(boundsElement);
    }

    /**
     * LEGACY METHOD: Encodes and saves locally, instead of uploading to the Cloud.
     *
     *
     * @param document The document of the level in question
     * @param levelID The level ID.
     * @param customLevelName The custom Level Name
     * @return True if successful, False otherwise.
     */
    private boolean saveDocumentToFile(Document document, LevelID levelID, String customLevelName) {
        try {
            // Compress the document before saving (modifies document in-place)
            Document compressedDocument = compressionHelper.compressXML(document);
            if (compressedDocument == null) {
                Log.e(TAG, "Compression failed - cancelling file save");
                return false;
            }

            // Create levels directory if it doesn't exist
            File levelsDir = new File(context.getFilesDir(), "levels");
            if (!levelsDir.exists()) {
                boolean succeeded = levelsDir.mkdirs();
                if(!succeeded) {
                    Log.i(TAG, "Issue creating path..");
                }
            }

            // Generate filename with random UID
            String baseName = (customLevelName != null && !customLevelName.trim().isEmpty())
                ? customLevelName.trim().replaceAll("[^a-zA-Z0-9_]", "_")
                : levelID.name();

            // Generate random UID (8 characters)
            String uid = generateRandomUID();
            String fileName = baseName + "_" + uid + ".xml";

            // Create the file
            File levelFile = new File(levelsDir, fileName);
            FileOutputStream outputStream = new FileOutputStream(levelFile);

            // Transform and save compressed document
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(compressedDocument);
            StreamResult result = new StreamResult(outputStream);
            transformer.transform(source, result);

            outputStream.close();
            return true;

        } catch (TransformerException | IOException e) {
            Log.e(TAG, "Error saving level to file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Encodes level data to compressed XML string for Firebase.
     * This method returns the compressed XML as a string without saving to file.
     *
     * @param levelID The level ID for metadata
     * @param simpleGridData Map of grid coordinates to SimplePhysicsObject instances
     * @param gridSize The size of each grid cell
     * @param playerSpawnX The player spawn X coordinate in grid units
     * @param playerSpawnY The player spawn Y coordinate in grid units
     * @return The compressed XML string, or null if encoding failed
     */
    public String encodeSimpleLevelToCompressedXML(LevelID levelID, Map<String, SimplePhysicsObject> simpleGridData, 
                                                   int gridSize, int playerSpawnX, int playerSpawnY) {
        if (simpleGridData != null && simpleGridData.size() > 10000) {
            Log.e(TAG, "Cannot encode level - object count (" + simpleGridData.size() + ") exceeds maximum limit of 10,000");
            return null;
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            // Create root element
            Element rootElement = document.createElement("level");
            document.appendChild(rootElement);

            // Add metadata
            addMetadata(document, rootElement, levelID, gridSize);
            
            // Add objects from SimpleModel grid data
            addObjectsFromSimpleGrid(document, rootElement, simpleGridData, gridSize, playerSpawnX, playerSpawnY);
            
            // Compress the document (modifies document in-place)
            Document compressedDocument = compressionHelper.compressXML(document);
            if (compressedDocument == null) {
                Log.e(TAG, "Compression failed - cancelling save");
                return null;
            }
            return compressionHelper.documentToString(compressedDocument);
            
        } catch (Exception e) {
            Log.e(TAG, "Error encoding level to compressed XML: " + e.getMessage());
            return null;
        }
    }

    private String generateRandomUID() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder uid = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < 8; i++) {
            uid.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return uid.toString();
    }

    /**
     * LEGACY METHOD: Encodes and saves locally, instead of uploading to the Cloud.
     *
     * @param document The XML document to save
     * @param levelID The level ID
     * @return true if save was successful
     */
    @SuppressWarnings("unused")
    private boolean saveDocumentToFile(Document document, LevelID levelID) {
        return saveDocumentToFile(document, levelID, null);
    }
} 
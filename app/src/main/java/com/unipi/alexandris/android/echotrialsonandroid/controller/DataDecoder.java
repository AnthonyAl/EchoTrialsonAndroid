package com.unipi.alexandris.android.echotrialsonandroid.controller;

import android.util.Log;

import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;
import com.unipi.alexandris.android.echotrialsonandroid.model.Block;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockCommon;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockIce;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpikeCommon;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpikeCommonDown;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpikeCommonLeft;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpikeCommonRight;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpikeIce;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpikeIceDown;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpikeIceLeft;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockSpikeIceRight;
import com.unipi.alexandris.android.echotrialsonandroid.model.BlockWater;
import com.unipi.alexandris.android.echotrialsonandroid.model.Player;
import com.unipi.alexandris.android.echotrialsonandroid.model.Portal;
import com.unipi.alexandris.android.echotrialsonandroid.model.Trigger;
import com.unipi.alexandris.android.echotrialsonandroid.model.TriggerFade;
import com.unipi.alexandris.android.echotrialsonandroid.model.TriggerGravity;
import com.unipi.alexandris.android.echotrialsonandroid.model.TriggerInversion;
import com.unipi.alexandris.android.echotrialsonandroid.model.TriggerMovement;
import com.unipi.alexandris.android.echotrialsonandroid.model.TriggerScale;
import com.unipi.alexandris.android.echotrialsonandroid.model.TriggerTranslate;
import com.unipi.alexandris.android.echotrialsonandroid.utility.Camera;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.model.PhysicsObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * DataDecoder class for handling XML decoding of level data.
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
public class DataDecoder {
    private static final String TAG = "DataDecoder";
    private final Camera camera;
    private final DataCompressionHelper compressionHelper;

    public DataDecoder(Camera camera) {
        this.camera = camera;
        this.compressionHelper = new DataCompressionHelper();
    }

    /**
     * Decodes level data from XML format to create game objects.
     * This method is called by LevelLoader to load a level from XML.
     *
     * @param document The raw level data
     * @return true if decoding was successful, false otherwise
     */
    public boolean decodeLevelFromXML(Document document) {
        try {
            Document decompressedDocument = compressionHelper.decompressXML(document);
            if (decompressedDocument == null) {
                Log.w(TAG, "Decompression failed, terminated loading process.");
                return false;
            }

            GameConstants.INSTANCE.getObjectHandler().clearObjects();
            loadObjectsFromXML(decompressedDocument);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error decoding level from file: " + e.getMessage());
            return false;
        }
    }

    private void loadObjectsFromXML(Document document) {
        Element rootElement = document.getDocumentElement();

        // Load player
        NodeList playerNodes = rootElement.getElementsByTagName("player");
        if (playerNodes.getLength() > 0) {
            loadPlayerFromXML((Element) playerNodes.item(0));
        }

        // Load blocks
        NodeList blocksNodes = rootElement.getElementsByTagName("blocks");
        if (blocksNodes.getLength() > 0) {
            Element blocksElement = (Element) blocksNodes.item(0);
            NodeList blockNodes = blocksElement.getElementsByTagName("block");

            for (int i = 0; i < blockNodes.getLength(); i++) {
                loadBlockFromXML((Element) blockNodes.item(i));
            }
        }

        NodeList individualBlockNodes = rootElement.getElementsByTagName("block");
        if (individualBlockNodes.getLength() > 0) {
            for (int i = 0; i < individualBlockNodes.getLength(); i++) {
                loadBlockFromXML((Element) individualBlockNodes.item(i));
            }
        }

        NodeList portalsNodes = rootElement.getElementsByTagName("portals");
        if (portalsNodes.getLength() > 0) {
            Element portalsElement = (Element) portalsNodes.item(0);
            NodeList portalNodes = portalsElement.getElementsByTagName("portal");

            for (int i = 0; i < portalNodes.getLength(); i++) {
                loadPortalFromXML((Element) portalNodes.item(i));
            }
        }

        NodeList triggersNodes = rootElement.getElementsByTagName("triggers");
        if (triggersNodes.getLength() > 0) {
            Element triggersElement = (Element) triggersNodes.item(0);
            NodeList triggerNodes = triggersElement.getElementsByTagName("trigger");

            Log.d(TAG, "Found " + triggerNodes.getLength() + " triggers to load");
            for (int i = 0; i < triggerNodes.getLength(); i++) {
                loadTriggerFromXML((Element) triggerNodes.item(i));
            }
        } else {
            Log.d(TAG, "No triggers found in level");
        }
    }

    private void loadPlayerFromXML(Element playerElement) {
        // Extract position
        NodeList positionNodes = playerElement.getElementsByTagName("position");
        if (positionNodes.getLength() > 0) {
            Element positionElement = (Element) positionNodes.item(0);
            double x = Double.parseDouble(positionElement.getAttribute("x"));
            double y = Double.parseDouble(positionElement.getAttribute("y"));

            int groupId = 0;
            NodeList groupIdNodes = playerElement.getElementsByTagName("groupId");
            if (groupIdNodes.getLength() > 0) {
                groupId = Integer.parseInt(groupIdNodes.item(0).getTextContent());
            }

            Player player = new Player(x, y, GameConstants.INSTANCE.getObjectHandler(),
                    camera, groupId);

            NodeList sizeNodes = playerElement.getElementsByTagName("size");
            if (sizeNodes.getLength() > 0) {
                int size = Integer.parseInt(sizeNodes.item(0).getTextContent());
                player.setSize(size);
            }

            NodeList physicsNodes = playerElement.getElementsByTagName("physics");
            if (physicsNodes.getLength() > 0) {
                Element physicsElement = (Element) physicsNodes.item(0);
                
                NodeList speedXNodes = physicsElement.getElementsByTagName("speedX");
                if (speedXNodes.getLength() > 0) {
                    double speedX = Double.parseDouble(speedXNodes.item(0).getTextContent());
                    player.setSpeedX(speedX);
                }
                
                NodeList speedYNodes = physicsElement.getElementsByTagName("speedY");
                if (speedYNodes.getLength() > 0) {
                    double speedY = Double.parseDouble(speedYNodes.item(0).getTextContent());
                    player.setSpeedY(speedY);
                }
                
                NodeList gravityNodes = physicsElement.getElementsByTagName("gravity");
                if (gravityNodes.getLength() > 0) {
                    double gravity = Double.parseDouble(gravityNodes.item(0).getTextContent());
                    player.setGravity(gravity);
                }
            }

            loadPhysicsProperties(player, playerElement);

            NodeList stateNodes = playerElement.getElementsByTagName("state");
            if (stateNodes.getLength() > 0) {
                Element stateElement = (Element) stateNodes.item(0);

                NodeList inAirNodes = stateElement.getElementsByTagName("inAir");
                if (inAirNodes.getLength() > 0) {
                    boolean inAir = Boolean.parseBoolean(inAirNodes.item(0).getTextContent());
                    player.setInAir(inAir);
                }

                NodeList onIceNodes = stateElement.getElementsByTagName("onIce");
                if (onIceNodes.getLength() > 0) {
                    boolean onIce = Boolean.parseBoolean(onIceNodes.item(0).getTextContent());
                    player.setOnIce(onIce);
                }
            }

            y = player.getY() - player.getHeight() + 48;
            player.setY(y);

            GameConstants.INSTANCE.setSpawnX((int)x);
            GameConstants.INSTANCE.setSpawnY((int)y);

            GameConstants.INSTANCE.getObjectHandler().addObject(player);
            GameConstants.INSTANCE.setPlayer(player);

            double centerX = player.getX() + (double) player.getSize() / 2 - (double) GameConstants.INSTANCE.getScreenWidth() / 3;
            double centerY = player.getY() + (double) player.getSize() / 2 - (double) GameConstants.INSTANCE.getScreenHeight() / 2;
            camera.setX(centerX);
            camera.setY(centerY);

        }
    }

    private void loadBlockFromXML(Element blockElement) {
        String blockType = blockElement.getAttribute("type");

        NodeList positionNodes = blockElement.getElementsByTagName("position");
        if (positionNodes.getLength() > 0) {
            Element positionElement = (Element) positionNodes.item(0);
            double x = Double.parseDouble(positionElement.getAttribute("x"));
            double y = Double.parseDouble(positionElement.getAttribute("y"));

            int groupId = 0;
            NodeList groupIdNodes = blockElement.getElementsByTagName("groupId");
            if (groupIdNodes.getLength() > 0) {
                groupId = Integer.parseInt(groupIdNodes.item(0).getTextContent());
            }

            Block block = createBlockByType(blockType, x, y, groupId);

            if (block != null) {
                NodeList sizeNodes = blockElement.getElementsByTagName("size");
                if (sizeNodes.getLength() > 0) {
                    int size = Integer.parseInt(sizeNodes.item(0).getTextContent());
                    block.setSize(size);
                }

                NodeList physicsNodes = blockElement.getElementsByTagName("physics");
                if (physicsNodes.getLength() > 0) {
                    Element physicsElement = (Element) physicsNodes.item(0);
                    loadPhysicsProperties(block, physicsElement);
                }

                GameConstants.INSTANCE.getObjectHandler().addObject(block);
            }
        }
    }

    private void loadPortalFromXML(Element portalElement) {
        NodeList positionNodes = portalElement.getElementsByTagName("position");
        if (positionNodes.getLength() > 0) {
            Element positionElement = (Element) positionNodes.item(0);
            double x = Double.parseDouble(positionElement.getAttribute("x"));
            double y = Double.parseDouble(positionElement.getAttribute("y"));

            int groupId = 0;
            NodeList groupIdNodes = portalElement.getElementsByTagName("groupId");
            if (groupIdNodes.getLength() > 0) {
                groupId = Integer.parseInt(groupIdNodes.item(0).getTextContent());
            }

            Portal portal = new Portal(x, y, GameConstants.INSTANCE.getObjectHandler(), groupId);

            NodeList physicsNodes = portalElement.getElementsByTagName("physics");
            if (physicsNodes.getLength() > 0) {
                Element physicsElement = (Element) physicsNodes.item(0);
                loadPhysicsProperties(portal, physicsElement);
            }

            GameConstants.INSTANCE.getObjectHandler().addObject(portal);
        }
    }

    private void loadPhysicsProperties(PhysicsObject object, Element physicsElement) {
        NodeList velXNodes = physicsElement.getElementsByTagName("velX");
        if (velXNodes.getLength() > 0) {
            double velX = Double.parseDouble(velXNodes.item(0).getTextContent());
            object.setVelX(velX);
        }
        
        NodeList velYNodes = physicsElement.getElementsByTagName("velY");
        if (velYNodes.getLength() > 0) {
            double velY = Double.parseDouble(velYNodes.item(0).getTextContent());
            object.setVelY(velY);
        }

        if (!(object instanceof Portal)) {
            NodeList widthNodes = physicsElement.getElementsByTagName("width");
            if (widthNodes.getLength() > 0) {
                double width = Double.parseDouble(widthNodes.item(0).getTextContent());
                object.setWidth(width);
            }
            
            NodeList heightNodes = physicsElement.getElementsByTagName("height");
            if (heightNodes.getLength() > 0) {
                double height = Double.parseDouble(heightNodes.item(0).getTextContent());
                object.setHeight(height);
            }
        }

        if (!(object instanceof Portal)) {
            NodeList widthMultiplierNodes = physicsElement.getElementsByTagName("widthMultiplier");
            if (widthMultiplierNodes.getLength() > 0) {
                double widthMultiplier = Double.parseDouble(widthMultiplierNodes.item(0).getTextContent());
                object.setWidthMultiplier(widthMultiplier);
            }
            
            NodeList heightMultiplierNodes = physicsElement.getElementsByTagName("heightMultiplier");
            if (heightMultiplierNodes.getLength() > 0) {
                double heightMultiplier = Double.parseDouble(heightMultiplierNodes.item(0).getTextContent());
                object.setHeightMultiplier(heightMultiplier);
            }
        }

        NodeList activeNodes = physicsElement.getElementsByTagName("active");
        if (activeNodes.getLength() > 0) {
            boolean active = Boolean.parseBoolean(activeNodes.item(0).getTextContent());
            object.setActive(active);
        }
        
        NodeList visibleNodes = physicsElement.getElementsByTagName("visible");
        if (visibleNodes.getLength() > 0) {
            boolean visible = Boolean.parseBoolean(visibleNodes.item(0).getTextContent());
            object.setVisible(visible);
        }
    }

    private Block createBlockByType(String blockType, double x, double y, int groupId) {
        ObjectHandler handler = GameConstants.INSTANCE.getObjectHandler();

        switch (blockType) {
            case "SimpleBlockCommon":
                return new BlockCommon(x, y, handler, groupId);
            case "SimpleBlockIce":
                return new BlockIce(x, y, handler, groupId);
            case "SimpleBlockWater":
                return new BlockWater(x, y, handler, groupId);
            case "SimpleBlockSpike":
            case "SimpleBlockSpikeCommon":
                return new BlockSpikeCommon(x, y, handler, groupId);
            case "SimpleBlockSpikeCommonDown":
                return new BlockSpikeCommonDown(x, y, handler, groupId);
            case "SimpleBlockSpikeCommonLeft":
                return new BlockSpikeCommonLeft(x, y, handler, groupId);
            case "SimpleBlockSpikeCommonRight":
                return new BlockSpikeCommonRight(x, y, handler, groupId);
            case "SimpleBlockSpikeIce":
                return new BlockSpikeIce(x, y, handler, groupId);
            case "SimpleBlockSpikeIceDown":
                return new BlockSpikeIceDown(x, y, handler, groupId);
            case "SimpleBlockSpikeIceLeft":
                return new BlockSpikeIceLeft(x, y, handler, groupId);
            case "SimpleBlockSpikeIceRight":
                return new BlockSpikeIceRight(x, y, handler, groupId);
            default:
                Log.w(TAG, "Unknown block type: " + blockType);
                return null;
        }
    }

    private void loadTriggerFromXML(Element triggerElement) {
        String triggerType = determineTriggerType(triggerElement);

        NodeList positionNodes = triggerElement.getElementsByTagName("position");
        if (positionNodes.getLength() > 0) {
            Element positionElement = (Element) positionNodes.item(0);
            double x = Double.parseDouble(positionElement.getAttribute("x"));
            double y = Double.parseDouble(positionElement.getAttribute("y"));

            int groupId = 0;
            NodeList groupIdNodes = triggerElement.getElementsByTagName("groupId");
            if (groupIdNodes.getLength() > 0) {
                groupId = Integer.parseInt(groupIdNodes.item(0).getTextContent());
            }

            int delay = 0, speed = 60, wr1 = 0, wr2 = 0, hr1 = 0, hr2 = 0;
            
            NodeList triggerNodes = triggerElement.getElementsByTagName("trigger");
            if (triggerNodes.getLength() > 0) {
                Element triggerSubElement = (Element) triggerNodes.item(0);

                NodeList delayNodes = triggerSubElement.getElementsByTagName("delay");
                if (delayNodes.getLength() > 0) {
                    delay = Integer.parseInt(delayNodes.item(0).getTextContent());
                }

                NodeList speedNodes = triggerSubElement.getElementsByTagName("speed");
                if (speedNodes.getLength() > 0) {
                    speed = Integer.parseInt(speedNodes.item(0).getTextContent());
                }

                NodeList wr1Nodes = triggerSubElement.getElementsByTagName("WR1");
                if (wr1Nodes.getLength() > 0) {
                    wr1 = Integer.parseInt(wr1Nodes.item(0).getTextContent());
                }
                
                NodeList wr2Nodes = triggerSubElement.getElementsByTagName("WR2");
                if (wr2Nodes.getLength() > 0) {
                    wr2 = Integer.parseInt(wr2Nodes.item(0).getTextContent());
                }
                
                NodeList hr1Nodes = triggerSubElement.getElementsByTagName("HR1");
                if (hr1Nodes.getLength() > 0) {
                    hr1 = Integer.parseInt(hr1Nodes.item(0).getTextContent());
                }
                
                NodeList hr2Nodes = triggerSubElement.getElementsByTagName("HR2");
                if (hr2Nodes.getLength() > 0) {
                    hr2 = Integer.parseInt(hr2Nodes.item(0).getTextContent());
                }
            }

            Trigger trigger = createTriggerByType(triggerType, x, y, groupId, delay, speed, wr1, wr2, hr1, hr2);

            if (trigger != null) {
                NodeList sizeNodes = triggerElement.getElementsByTagName("size");
                if (sizeNodes.getLength() > 0) {
                    int size = Integer.parseInt(sizeNodes.item(0).getTextContent());
                    trigger.setSize(size);
                }

                NodeList physicsNodes = triggerElement.getElementsByTagName("physics");
                if (physicsNodes.getLength() > 0) {
                    Element physicsElement = (Element) physicsNodes.item(0);
                    loadPhysicsProperties(trigger, physicsElement);
                }

                loadTriggerSpecificProperties(trigger, triggerElement);

                GameConstants.INSTANCE.getObjectHandler().addObject(trigger);

                Log.d(TAG, "Loaded trigger: " + triggerType + " at (" + x + ", " + y + ") with bounds WR1=" + wr1 + ", WR2=" + wr2 + ", HR1=" + hr1 + ", HR2=" + hr2);
                Log.d(TAG, "Trigger active: " + trigger.isActive() + ", visible: " + trigger.isVisible() + ", total objects: " + GameConstants.INSTANCE.getObjectHandler().getObjectsView().size());
            }
        }
    }
    
    /**
     * Determines the trigger type based on the presence of specific property sections.
     * 
     * @param triggerElement The trigger element from XML
     * @return The trigger type string, or null if type cannot be determined
     */
    private String determineTriggerType(Element triggerElement) {
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

        return "SimpleTriggerInversion";
    }

    private Trigger createTriggerByType(String triggerType, double x, double y, int groupId, int delay, int speed, int wr1, int wr2, int hr1, int hr2) {
        ObjectHandler handler = GameConstants.INSTANCE.getObjectHandler();

        switch (triggerType) {
            case "SimpleTriggerFade":
                return new TriggerFade((int)x, (int)y, handler, delay, speed, wr1, wr2, hr1, hr2, groupId);
            case "SimpleTriggerGravity":
                return new TriggerGravity((int)x, (int)y, handler, delay, speed, wr1, wr2, hr1, hr2, groupId);
            case "SimpleTriggerInversion":
                return new TriggerInversion((int)x, (int)y, handler, delay, speed, wr1, wr2, hr1, hr2, groupId);
            case "SimpleTriggerMovement":
                return new TriggerMovement((int)x, (int)y, handler, delay, speed, wr1, wr2, hr1, hr2, groupId);
            case "SimpleTriggerScale":
                return new TriggerScale((int)x, (int)y, handler, delay, speed, wr1, wr2, hr1, hr2, groupId);
            case "SimpleTriggerTranslate":
                return new TriggerTranslate((int)x, (int)y, handler, delay, speed, wr1, wr2, hr1, hr2, groupId);
            default:
                Log.w(TAG, "Unknown trigger type: " + triggerType);
                return null;
        }
    }

    private void loadTriggerSpecificProperties(Trigger trigger, Element triggerElement) {
        String triggerType = determineTriggerType(triggerElement);

        switch (triggerType) {
            case "SimpleTriggerFade":
                loadFadeTriggerProperties((TriggerFade) trigger, triggerElement);
                break;
            case "SimpleTriggerGravity":
                loadGravityTriggerProperties((TriggerGravity) trigger, triggerElement);
                break;
            case "SimpleTriggerMovement":
                loadMovementTriggerProperties((TriggerMovement) trigger, triggerElement);
                break;
            case "SimpleTriggerScale":
                loadScaleTriggerProperties((TriggerScale) trigger, triggerElement);
                break;
            case "SimpleTriggerTranslate":
                loadTranslateTriggerProperties((TriggerTranslate) trigger, triggerElement);
                break;
        }
    }

    private void loadFadeTriggerProperties(TriggerFade trigger, Element triggerElement) {
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

    private void loadGravityTriggerProperties(TriggerGravity trigger, Element triggerElement) {
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
                String direction = directionNodes.item(0).getTextContent();
                trigger.setGravityDirection(TriggerGravity.GravityDirection.valueOf(direction));
            }
        }
    }

    private void loadMovementTriggerProperties(TriggerMovement trigger, Element triggerElement) {
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

    private void loadScaleTriggerProperties(TriggerScale trigger, Element triggerElement) {
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

    private void loadTranslateTriggerProperties(TriggerTranslate trigger, Element triggerElement) {
        NodeList translateNodes = triggerElement.getElementsByTagName("translate");
        if (translateNodes.getLength() > 0) {
            Element translateElement = (Element) translateNodes.item(0);
            
            NodeList directionNodes = translateElement.getElementsByTagName("direction");
            if (directionNodes.getLength() > 0) {
                String direction = directionNodes.item(0).getTextContent();
                trigger.setDirection(TriggerTranslate.Direction.valueOf(direction));
            }
            
            NodeList blockCountNodes = translateElement.getElementsByTagName("blockCount");
            if (blockCountNodes.getLength() > 0) {
                int blockCount = Integer.parseInt(blockCountNodes.item(0).getTextContent());
                trigger.setBlockCount(blockCount);
            }
        }
    }
}
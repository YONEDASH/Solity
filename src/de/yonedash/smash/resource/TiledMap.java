package de.yonedash.smash.resource;

import de.yonedash.smash.*;
import de.yonedash.smash.entity.LevelObject;
import de.yonedash.smash.entity.Tile;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class TiledMap implements ProgressReport {

    private final String path;
    private LevelData levelData;

    private int totalProgress, progress;

    public TiledMap(String path) {
        this.path = path;
    }

    public LevelData load(TextureAtlas atlas) {
        // Load file as document
        InputStream stream = ResourceFinder.openInputStream(path);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document document;
        try {
            document = documentBuilder.parse(stream);
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }

        try {
            assert stream != null;
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Create map in which we save the tile id's with their corresponding source image name
        ArrayList<String> imageSources = new ArrayList<>();
        HashMap<String, Integer> sourceGid = new HashMap<>();

        // Get this data from file, get every node with name "tileset"
        NodeList tilesetData = document.getElementsByTagName("tileset");

        // Set total progress for reporting
        // Each tileset times 2 for - finding, loading individual tiles + layer count
        this.totalProgress = tilesetData.getLength() * 2 + document.getElementsByTagName("layer").getLength();

        // Loop through each of them
        for (int i = 0; i < tilesetData.getLength(); i++) {
            // Get node
            Node node = tilesetData.item(i);
            NamedNodeMap nodeMap = node.getAttributes();

            // Pre-define values
            String source = null;
            int gid = -1;

            // Loop through each attribute to get data
            for (int j = 0; j < nodeMap.getLength(); j++) {
                Node attribute = nodeMap.item(j);
                String name = attribute.getNodeName();
                String value = attribute.getNodeValue();

                // Set pre-defined values
                if (name.equals("firstgid"))
                    gid = Integer.parseInt(value);
                else if (name.equals("source"))
                    source = value;
            }

            // If not every value was set, there is a problem with the map file,
            // and it is not worth to keep loading - return null
            if (source == null || gid == -1)
                return null;

            // Replace .tsx with .png in order to grab image file
            String imageSource = source.replace(".tsx", ".png");
            imageSources.add(imageSource);

            sourceGid.put(imageSource, gid);

            this.progress++;
        }

        // Now load textures

        // Create map of every image source with textures and their ids
        HashMap<String, HashMap<Integer, Texture>> textures = new HashMap<>();

        int tileSize = 16;
        String texturePrefix = "/";

        // Loop through every image source
        for (String imageSource : imageSources) {
            // Load source as texture
            Texture sourceTexture = atlas.loadTexture(texturePrefix + imageSource);
            BufferedImage bi = sourceTexture.getBufferedImage();

            // Create map with id and their corresponding texture
            HashMap<Integer, Texture> ids = new HashMap<>();
            textures.put(imageSource, ids);

            // Convert image source to single textures/tiles
            int w = bi.getWidth() / tileSize;
            for (int i = 0; i < w * (bi.getHeight() / tileSize); i++) {
                // Convert index to x and y position
                int y = i / w;
                int x = i - (y * w);

                // Load & save texture with id
                ids.put(i, atlas.loadTexture(sourceTexture, x * tileSize, y * tileSize, tileSize, tileSize));
            }

            // Flush source texture since we don't need it anymore
            sourceTexture.flush();

            this.progress++;
        }

        ArrayList<LevelObject> tiles = new ArrayList<>();
        int tileMapSize = LevelObject.TILE_SIZE;

        // Get this data from file, get every node with name "layer"
        NodeList layerData = document.getElementsByTagName("layer");
        // Loop through each of them
        for (int i = 0; i < layerData.getLength(); i++) {
            // Get node
            Node node = layerData.item(i);
            NamedNodeMap nodeMap = node.getAttributes();

            // Pre-define values
            int id = -1;
            int layerOffsetX = 0, layerOffsetY = 0;

            // Loop through each attribute to get data
            for (int j = 0; j < nodeMap.getLength(); j++) {
                Node attribute = nodeMap.item(j);
                String name = attribute.getNodeName();
                String value = attribute.getNodeValue();

                // Set pre-defined values
                if (name.equals("id"))
                    id = Integer.parseInt(value);
                if (name.equals("offsetx"))
                    layerOffsetX = Integer.parseInt(value);
                if (name.equals("offsety"))
                    layerOffsetY = Integer.parseInt(value);
            }

            // If not every value was set, there is a problem with the map file,
            // and it is not worth to keep loading - return null
            // Layer offsets are optional
            if (id == -1)
                return null;

            // Get children and find chunks
            NodeList childNodes = node.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node childNode = childNodes.item(j);

                if (!childNode.getNodeName().equals("data"))
                    continue;

                NodeList dataChildNodes = childNode.getChildNodes();
                for (int l = 0; l < dataChildNodes.getLength(); l++) {
                    Node dataChildNode = dataChildNodes.item(l);

                    if (dataChildNode.getNodeName().equals("chunk")) {
                        NamedNodeMap chunkAttributes = dataChildNode.getAttributes();

                        // Pre-define values
                        int offsetX = Integer.MIN_VALUE, offsetY = Integer.MIN_VALUE;

                        // Loop through each attribute to get data
                        for (int k = 0; k < chunkAttributes.getLength(); k++) {
                            Node attribute = chunkAttributes.item(k);
                            String name = attribute.getNodeName();
                            String value = attribute.getNodeValue();


                            // Set pre-defined values
                            if (name.equals("x"))
                                offsetX = Integer.parseInt(value);
                            else if (name.equals("y"))
                                offsetY = Integer.parseInt(value);
                        }

                        // If not every value was set, there is a problem with the map file,
                        // and it is not worth to keep loading - return null
                        if (offsetX == Integer.MIN_VALUE || offsetY == Integer.MIN_VALUE)
                            return null;

                        String value = dataChildNode.getTextContent();

                        int y = 0;
                        for (String line : value.split("\n")) {
                            if (line.isEmpty())
                                continue;

                            int x = 0;
                            for (String idString : line.split(",")) {

                                int tileId = Integer.parseInt(idString);

                                if (tileId == 0) {
                                    x++;
                                    continue;
                                }

                                String source = null;
                                int gidSource = 0;
                                for (String src : sourceGid.keySet()) {
                                    int gid = sourceGid.get(src);
                                    if (gid >= gidSource && gid <= tileId) {
                                        gidSource = gid;
                                        source = src;
                                    }
                                }
                                int sourceId = tileId - gidSource;
                                Texture texture = textures.get(source).get(sourceId);

                                // If texture is blank, do not add tile because we won't see it anyways
                                if (texture.isBlank()) {
                                    x++;
                                    continue;
                                }

                                int z = id - 3;
                                Tile tile = new Tile(new BoundingBox(
                                        new Vec2D(
                                                tileMapSize * (x + offsetX + layerOffsetX),
                                                tileMapSize * (y + offsetY + layerOffsetY)
                                        ),
                                        new Vec2D(tileMapSize, tileMapSize)),
                                        z,
                                        texture);

                                assert source != null;
                                updateCollision(source, sourceId, tile);
                                updateDynamicState(source, sourceId, tile);
                                updateParticleType(atlas, source, sourceId, tile);
                                tiles.add(tile);

                                x++;
                            }
                            y++;
                        }
                    }
                }
            }
            this.progress++;
        }

        try {
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this.levelData = new LevelData(tiles);
    }

    final BoundingBox full = new BoundingBox(Vec2D.zero(), new Vec2D(Tile.TILE_SIZE, Tile.TILE_SIZE));

    private void updateCollision(String source, int tileId, Tile tile) {
        BoundingBox origin = tile.getBoundingBox();
        if (source.equals("Cliff.png")) {
            if (tileId == 33) {
                tile.setCollisionBoxes(new BoundingBox[]{
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(0, Tile.TILE_SIZE * 0.2)), origin.size.clone().add(new Vec2D(0, -Tile.TILE_SIZE * 0.2)))
                });
            } else if (tileId == 19) {
                tile.setCollisionBoxes(new BoundingBox[]{
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(0, Tile.TILE_SIZE * 0.15)), new Vec2D(Tile.TILE_SIZE, Tile.TILE_SIZE * 0.3))
                });
            } else if (tileId == 25) {
                tile.setCollisionBoxes(new BoundingBox[]{
                        new BoundingBox(
                                origin.position.clone(), new Vec2D(Tile.TILE_SIZE * 0.5, Tile.TILE_SIZE))
                });
            } else if (tileId == 27) {
                tile.setCollisionBoxes(new BoundingBox[]{
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.5, 0)), new Vec2D(Tile.TILE_SIZE * 0.5, Tile.TILE_SIZE))
                });
            } else if (tileId == 18) {
                tile.setCollisionBoxes(new BoundingBox[]{
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.3)), new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.2)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.3, Tile.TILE_SIZE * 0.4)), new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.1)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.5, Tile.TILE_SIZE * 0.2)), new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.3)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.5)), new Vec2D(Tile.TILE_SIZE * 0.3, Tile.TILE_SIZE * 0.1)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.6, Tile.TILE_SIZE * 0.2)), new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.25)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.6)), new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.4))
                });
            } else if (tileId == 20) {
                tile.setCollisionBoxes(new BoundingBox[]{
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.4 - Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.3)), new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.2)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.3 - Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.4)), new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.1)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.5 - Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.2)), new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.3)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.2 - Tile.TILE_SIZE * 0.3, Tile.TILE_SIZE * 0.5)), new Vec2D(Tile.TILE_SIZE * 0.3, Tile.TILE_SIZE * 0.1)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.6 - Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.2)), new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.25)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.1 - Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.6)), new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.4))
                });
            } else if (tileId == 34) {
                tile.setCollisionBoxes(new BoundingBox[]{
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.0)), new Vec2D(Tile.TILE_SIZE * 0.6, Tile.TILE_SIZE * 0.2)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.2)), new Vec2D(Tile.TILE_SIZE * 0.55, Tile.TILE_SIZE * 0.25)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.45)), new Vec2D(Tile.TILE_SIZE * 0.5, Tile.TILE_SIZE * 0.1)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.55)), new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.1)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.65)), new Vec2D(Tile.TILE_SIZE * 0.3, Tile.TILE_SIZE * 0.15)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.0, Tile.TILE_SIZE * 0.8)), new Vec2D(Tile.TILE_SIZE * 0.6, Tile.TILE_SIZE * 0.1)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.1)), new Vec2D(Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.7)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.0, Tile.TILE_SIZE * 0.2)), new Vec2D(Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.6)),
                });
            } else if (tileId == 32) {
                tile.setCollisionBoxes(new BoundingBox[]{
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.4 - Tile.TILE_SIZE * 0.6, Tile.TILE_SIZE * 0.0)), new Vec2D(Tile.TILE_SIZE * 0.6, Tile.TILE_SIZE * 0.2)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.4 - Tile.TILE_SIZE * 0.55, Tile.TILE_SIZE * 0.2)), new Vec2D(Tile.TILE_SIZE * 0.55, Tile.TILE_SIZE * 0.25)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.4 - Tile.TILE_SIZE * 0.5, Tile.TILE_SIZE * 0.45)), new Vec2D(Tile.TILE_SIZE * 0.5, Tile.TILE_SIZE * 0.1)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.4 - Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.55)), new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.1)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.4 - Tile.TILE_SIZE * 0.3, Tile.TILE_SIZE * 0.65)), new Vec2D(Tile.TILE_SIZE * 0.3, Tile.TILE_SIZE * 0.15)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.0 - Tile.TILE_SIZE * 0.6, Tile.TILE_SIZE * 0.8)), new Vec2D(Tile.TILE_SIZE * 0.6, Tile.TILE_SIZE * 0.1)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.2 - Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.1)), new Vec2D(Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.7)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.0 - Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.2)), new Vec2D(Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.6)),
                });
            } else if (tileId == 5) {
                tile.setCollisionBoxes(new BoundingBox[]{
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.5, Tile.TILE_SIZE * 0.6)), new Vec2D(Tile.TILE_SIZE * 0.5, Tile.TILE_SIZE * 0.4)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.6, Tile.TILE_SIZE * 0.5)), new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.1)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.7, Tile.TILE_SIZE * 0.4)), new Vec2D(Tile.TILE_SIZE * 0.3, Tile.TILE_SIZE * 0.1)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.8, Tile.TILE_SIZE * 0.3)), new Vec2D(Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.1)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.9, Tile.TILE_SIZE * 0.2)), new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.1))
                });

            } else if (tileId == 6) {
                tile.setCollisionBoxes(new BoundingBox[]{
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.5 - Tile.TILE_SIZE * 0.5, Tile.TILE_SIZE * 0.6)), new Vec2D(Tile.TILE_SIZE * 0.5, Tile.TILE_SIZE * 0.4)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.6 - Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.5)), new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.1)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.7 - Tile.TILE_SIZE * 0.3, Tile.TILE_SIZE * 0.4)), new Vec2D(Tile.TILE_SIZE * 0.3, Tile.TILE_SIZE * 0.1)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.8 - Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.3)), new Vec2D(Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.1)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.9 - Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.2)), new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.1))
                });
            } else if (tileId == 13) {
                tile.setCollisionBoxes(new BoundingBox[]{
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D( Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.0)), new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.2)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D( Tile.TILE_SIZE * 0.0, Tile.TILE_SIZE * 0.1)), new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.3)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D( Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.2)), new Vec2D(Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.2)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D( Tile.TILE_SIZE * 0.3, Tile.TILE_SIZE * 0.2)), new Vec2D(Tile.TILE_SIZE * 0.15, Tile.TILE_SIZE * 0.15)),

                });
            } else if (tileId == 12) {
                tile.setCollisionBoxes(new BoundingBox[]{
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D( Tile.TILE_SIZE - Tile.TILE_SIZE * 0.1 - Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.0)), new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.2)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D( Tile.TILE_SIZE - Tile.TILE_SIZE * 0.0 - Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.1)), new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.3)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D( Tile.TILE_SIZE - Tile.TILE_SIZE * 0.1 - Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.2)), new Vec2D(Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.2)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D( Tile.TILE_SIZE - Tile.TILE_SIZE * 0.3 - Tile.TILE_SIZE * 0.15, Tile.TILE_SIZE * 0.2)), new Vec2D(Tile.TILE_SIZE * 0.15, Tile.TILE_SIZE * 0.15)),

                });
            }
//            tile.setCollisionBoxes(new BoundingBox[]{origin.clone()});
        } else if (source.equals("Trees.png")) {
            if (tileId == 80 || tileId == 85 || tileId == 89) {
                tile.setCollisionBoxes(new BoundingBox[]{
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.4)), new Vec2D(Tile.TILE_SIZE * 0.8, Tile.TILE_SIZE * 0.4))
                });
            }
        } else if (source.equals("RocksnStumps.png")) {
            if (tileId == 6) {
                tile.setCollisionBoxes(new BoundingBox[]{
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.7, Tile.TILE_SIZE * 0.1)), new Vec2D(Tile.TILE_SIZE * 0.3, Tile.TILE_SIZE * 0.65)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.5, Tile.TILE_SIZE * 0.1)), new Vec2D(Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.55)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.4, Tile.TILE_SIZE * 0.1)), new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.5)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.3, Tile.TILE_SIZE * 0.1)), new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.35)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.1)), new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.3))
                });
            } else if (tileId == 7) {
                tile.setCollisionBoxes(new BoundingBox[]{
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.7 - Tile.TILE_SIZE * 0.3, Tile.TILE_SIZE * 0.1)), new Vec2D(Tile.TILE_SIZE * 0.3, Tile.TILE_SIZE * 0.65)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.5 - Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.1)), new Vec2D(Tile.TILE_SIZE * 0.2, Tile.TILE_SIZE * 0.55)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.4 - Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.1)), new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.5)),
                        new BoundingBox(
                                origin.position.clone().add(new Vec2D(Tile.TILE_SIZE - Tile.TILE_SIZE * 0.3 - Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.1)), new Vec2D(Tile.TILE_SIZE * 0.1, Tile.TILE_SIZE * 0.35))
                });
            }
        }
    }

    private void updateDynamicState(String source, int tileId, Tile tile) {
        if (source.equals("Trees.png")) {
            if (tileId == 80 || tileId == 85 || tileId == 89) {
                tile.setDynamic(0.6);
            }
        } else if (source.equals("RocksnStumps.png")) {
            if (tileId == 0 || tileId == 1 || tileId == 5
                    ||  tileId == 8 || tileId == 10 || tileId == 11 || tileId == 13 ) {
                tile.setDynamic(0.6);
            } else if (tileId == 2 || tileId == 3) {
                tile.setDynamic(1.2);
            } else if (tileId == 4 || tileId == 9) {
                tile.setDynamic(0.6);
            } else if (tileId == 6 || tileId == 7) {
                tile.setDynamic(0.2);
            } else {
                tile.setZ(0);
            }
        }
    }

    private void updateParticleType(TextureAtlas atlas, String source, int tileId, Tile tile) {
        if (source.equals("Trees.png")) {
            // Exclude tree trunks
            if (tileId != 80 && tileId != 85 && tileId != 89
             && tileId != 67 && tileId != 54 && tileId != 72) {
                tile.setParticleType(atlas, ParticleType.LEAF);
            }
        }
    }

    public LevelData getLevelData() {
        return this.levelData;
    }

    @Override
    public int getProgress() {
        return this.progress;
    }

    @Override
    public int getProgressTotal() {
        return this.totalProgress;
    }

}

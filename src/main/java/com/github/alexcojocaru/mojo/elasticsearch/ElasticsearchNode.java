package com.github.alexcojocaru.mojo.elasticsearch;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

/**
 * @author alexcojocaru
 *
 */
public class ElasticsearchNode
{
    private Node node;
    private int httpPort;

    /**
     * Start a local ES node with the given settings.
     * <br>
     * If the local node is already running prior to calling this method,
     * an IllegalStateException will be thrown.
     * @param settings
     * @throws MojoExecutionException 
     */
    public ElasticsearchNode(Settings settings) throws MojoExecutionException
    {
        // Set the node to be as lightweight as possible,
        // at the same time being able to be discovered from an external JVM.
        settings = Settings.settingsBuilder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 0)
                .put("network.host", "127.0.0.1")
                .put("discovery.zen.ping.timeout", "3ms")
                .put("discovery.zen.ping.multicast.enabled", false)
                .put("http.cors.enabled", true)
                .put(settings)
                .build();
        
        httpPort = settings.getAsInt("http.port", 9200);
        
        node = NodeBuilder.nodeBuilder().settings(settings).node();
    }
    
    /**
     * Start a local ES node with default settings.
     * <br>
     * If the local node is already running prior to calling this method,
     * an IllegalStateException will be thrown.
     * @param dataPath
     * @throws MojoExecutionException 
     * @return an instance of an ElasticsearchNode
     */
    public static ElasticsearchNode start(String dataPath) throws MojoExecutionException
    {
        // ES v2.0.0 requires the path.home property.
        // Set it to the parent of the data directory.
        String homePath = new File(dataPath).getParent();
        
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "test")
                .put("action.auto_create_index", false)
                .put("transport.tcp.port", 9300)
                .put("http.port", 9200)
                .put("path.data", dataPath)
                .put("path.home", homePath)
                .build();
        return new ElasticsearchNode(settings);
    }

    /**
     * Always return a new client connected to the local ES node.
     * @return
     */
    public Client getClient()
    {
        return node.client();
    }
    
    /**
     * Close the ES node.
     * <br>
     * If the node is already stopped or closed, this method is a no-op.
     */
    public void stop()
    {
        if (node != null)
        {
            node.close();
            node = null;
        }
    }

    public boolean isClosed()
    {
        return (node == null || node != null && node.isClosed());
    }

    /**
     * @return the httpPort
     */
    public int getHttpPort()
    {
        return httpPort;
    }

}

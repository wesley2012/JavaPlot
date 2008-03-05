/*
 * GNUPlotParameters.java
 *
 * Created on October 13, 2007, 4:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.panayotis.gnuplot;

import com.panayotis.gnuplot.layout.GraphLayout;
import com.panayotis.gnuplot.layout.GridGraphLayout;
import com.panayotis.gnuplot.layout.LayoutMetrics;
import com.panayotis.gnuplot.plot.Axis;
import com.panayotis.gnuplot.plot.Graph;
import com.panayotis.gnuplot.plot.Plot;
import com.panayotis.gnuplot.terminal.GNUPlotTerminal;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This is a placeholder for the parameters used to create the actual plot.
 * 
 * @author teras
 */
public class GNUPlotParameters extends PropertiesHolder implements Serializable {

    public final static String ERRORTAG = "_ERROR_";
    public final static String ERROR_VAR = "_gnuplot_error";
    public final static String NOERROR_COMMAND = " ; " + ERROR_VAR + " = 0";
    private ArrayList<Graph> graphs;
    private String pagetitle;
    private GraphLayout layout;
    private int defaultgraph;
    private ArrayList<String> preinit;
    private ArrayList<String> postinit;

    /**
     *  Create a new plot with the default parameters
     */
    public GNUPlotParameters() {
        graphs = new ArrayList<Graph>();
        graphs.add(new Graph());
        pagetitle = "";
        layout = new GridGraphLayout(1, 1);
        defaultgraph = 0;

        preinit = new ArrayList<String>();
        postinit = new ArrayList<String>();
    }

    /**
     *  Get one of the available Axis from default Graph, in orde to set some parameters on it.
     * @param axisname The name of the Axis. It is usually "x", "y", "z"
     * @return The desired Axis
     */
    public Axis getAxis(String axisname) {
        return graphs.get(defaultgraph).getAxis(axisname);
    }

    /**
     * This list is used to add special commands to gnuplot, before the automatically
     * generated from this library. It is a convenient method to send unsupported commands
     * to gnuplot at the beginning of the program.
     * @return The list of the initialization commands
     */
    public ArrayList<String> getPreInit() {
        return preinit;
    }

    /**
     * This list is used to add special commands to gnuplot, after the automatically
     * generated from this library. It is a convenient method to send unsupported commands
     * to gnuplot at the end of the program, just before the final plot command.
     * @return he list of the post initialization commands
     */
    public ArrayList<String> getPostInit() {
        return postinit;
    }

    /**
     * Add a new plot to the default plot group.
     * At least one plot is needed to produce visual results.
     * @param plot The given plot.
     */
    public void addPlot(Plot plot) {
        graphs.get(defaultgraph).add(plot);
    }

    /**
     * Add a new Graph object. This method is used to create a multiplot graph.
     * Every "plot" command corresponds to a different Graph object. In order to
     * draw to a new plot gnuplot object, create a new page.
     */
    public void newGraph() {
        addGraph(new Graph());
    }

    /**
     * Add a defined graph.
     * @param gr Graph object to be added
     * @see #newGraph()
     */
    public void addGraph(Graph gr) {
        graphs.add(gr);
        defaultgraph = graphs.size() - 1;
        layout.updateCapacity(graphs.size());
    }

    /**
     * Set the title of all graph objects, in multiplot environment.
     * @param title The title to use
     */
    public void setMultiTitle(String title) {
        if (title==null) title = "";
        pagetitle = title;
    }
    
    /**
     * Get the actual GNUPlot commands. This method is used to construct the gnuplot program
     * @param term The terminal to use
     * @return The GNUPlot program
     */
    String getPlotCommands(GNUPlotTerminal term) {
        StringBuffer bf = new StringBuffer();

        /* First execute pre-init commands */
        for (String com : preinit) {
            bf.append(com).append(NL);
        }


        /* Gather various "set" parameters */
        appendProperties(bf);

        /* Set Terminal (and it's parameters) */
        if (!term.getType().equals("")) {
            bf.append("set term ").append(term.getType()).append(NL);
        }
        if (!term.getOutputFile().equals("")) {
            bf.append("set output \'").append(term.getOutputFile()).append("\'").append(NL);
        }


        /* We are almost ready. Before executing the actual plot command, issue the post-init commands */
        for (String com : postinit) {
            bf.append(com).append(NL);
        }

        /* Append various plots */
        if (graphs.size() > 1) {
            /* This is a multiplot */
            bf.append("set multiplot");
            if (!pagetitle.equals(""))
                    bf.append(" title \"").append(pagetitle).append('"');
            bf.append(NL);

            LayoutMetrics metrics;
            for (int i = 0; i < graphs.size(); i++) {
                metrics = layout.getMetrics(i);
                bf.append("set origin ").append(metrics.x).append(',').append(metrics.y).append(NL);
                bf.append("set size ").append(metrics.width).append(',').append(metrics.height).append(NL);
                graphs.get(i).retrieveData(bf);
            }

            bf.append("unset multiplot").append(NL);
        } else {
            graphs.get(0).retrieveData(bf);
        }

        /* Finish! */
        bf.append("quit").append(NL);

        return bf.toString();
    }

    /**
     * Get the list of the stored graphs
     * @return List of Graph objects
     */
    public ArrayList<Graph> getGraphs() {
        return graphs;
    }

    /**
     * Get the list of the stored plots from default graph
     * @return List of Plot objects
     */
    public ArrayList<Plot> getPlots() {
        return graphs.get(defaultgraph);
    }
}

package com.mongodb.casbah

import com.mongodb.casbah.Imports._

import scala.collection.JavaConverters._

import com.mongodb.{ AggregationOutput => JavaAggregationOutput }


/**
 * Wrapper object for AggregationOutput
 *
 * @since 2.5
 */
class AggregationOutput(underlying: JavaAggregationOutput) {

    /**
     * returns an iterator to the results of the aggregation
     * @return
     */
    def results = underlying.results.asScala

    /**
     * returns the command result of the aggregation
     * @return
     */
    def getCommandResult = underlying.getCommandResult.asScala

    /**
     * returns the command result of the aggregation
     * @return
     */
    def commandResult = underlying.getCommandResult.asScala

    /**
     * returns the original aggregation command
     * @return
     */
    def getCommand = underlying.getCommand

    /**
     * returns the original aggregation command
     * @return
     */
    def command = underlying.getCommand

    /**
     * returns the address of the server used to execute the aggregation
     * @return
     */
    def getServerUsed() = underlying.getServerUsed

    /**
     * returns the address of the server used to execute the aggregation
     * @return
     */
    def serverUsed() = underlying.getServerUsed

    /**
     * string representation of the aggregation command
     */
    override def toString() = underlying.toString

}
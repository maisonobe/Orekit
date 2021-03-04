/* Copyright 2002-2021 CS GROUP
 * Licensed to CS GROUP (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.files.ccsds.ndm.odm;

import org.orekit.files.ccsds.utils.lexical.ParseToken;
import org.orekit.files.ccsds.utils.lexical.TokenType;
import org.orekit.files.ccsds.utils.parsing.ParsingContext;


/** Keys for {@link StateVector ODM state vector data} entries.
 * @author Luc Maisonobe
 * @since 11.0
 */
public enum StateVectorKey {

    /** Comment entry. */
    COMMENT((token, context, data) ->
            token.getType() == TokenType.ENTRY ? data.addComment(token.getContent()) : true),

    /** Epoch of state vector and optional Keplerian elements. */
    EPOCH((token, context, data) -> token.processAsDate(data::setEpoch, context)),

    /** Position vector X-component. */
    X((token, context, data) -> token.processAsIndexedDouble(0, 1.0e3, data::setP)),

    /** Position vector Y-component. */
    Y((token, context, data) -> token.processAsIndexedDouble(1, 1.0e3, data::setP)),

    /** Position vector Z-component. */
    Z((token, context, data) -> token.processAsIndexedDouble(2, 1.0e3, data::setP)),

    /** Velocity vector X-component. */
    X_DOT((token, context, data) -> token.processAsIndexedDouble(0, 1.0e3, data::setV)),

    /** Velocity vector Y-component. */
    Y_DOT((token, context, data) -> token.processAsIndexedDouble(1, 1.0e3, data::setV)),

    /** Velocity vector Z-component. */
    Z_DOT((token, context, data) -> token.processAsIndexedDouble(2, 1.0e3, data::setV));

    /** Processing method. */
    private final TokenProcessor processor;

    /** Simple constructor.
     * @param processor processing method
     */
    StateVectorKey(final TokenProcessor processor) {
        this.processor = processor;
    }

    /** Process one token.
     * @param token token to process
     * @param context parsing context
     * @param data data to fill
     * @return true of token was accepted
     */
    public boolean process(final ParseToken token, final ParsingContext context, final StateVector data) {
        return processor.process(token, context, data);
    }

    /** Interface for processing one token. */
    interface TokenProcessor {
        /** Process one token.
         * @param token token to process
         * @param context parsing context
         * @param data data to fill
         * @return true of token was accepted
         */
        boolean process(ParseToken token, ParsingContext context, StateVector data);
    }

}
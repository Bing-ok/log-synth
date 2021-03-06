/*
 * Licensed to the Ted Dunning under one or more contributor license
 * agreements.  See the NOTICE file that may be
 * distributed with this work for additional information
 * regarding copyright ownership.  Ted Dunning licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.mapr.synth.samplers;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SsnSamplerTest {
    private static final int N = 50000;

    @Test
    public void testSsns() throws IOException {
        SchemaSampler s = SchemaSampler.fromResource("schema019.json");

        JsonNode v = s.sample();
        // regression test given that we specify the seed
        assertEquals("573-87-1992", v.get("z").get("ssn").asText());

        Multiset<String> type1 = HashMultiset.create();
        Multiset<String> type2 = HashMultiset.create();
        Multiset<String> state1 = HashMultiset.create();
        Multiset<String> state2 = HashMultiset.create();

        String legalSsn = "\\d\\d\\d-\\d\\d-\\d\\d\\d\\d";

        for (int i = 0; i < N; i++) {
            v = s.sample();
            type1.add(v.get("z").get("type").asText());
            type2.add(v.get("zLimited").get("type").asText());

            state1.add(v.get("z").get("state").asText());
            state2.add(v.get("zLimited").get("state").asText());

            assertTrue(String.format("Bad format for SSN: %s", v.get("z").get("ssn")), v.get("z").get("ssn").asText().matches(legalSsn));
            assertTrue(v.get("zFlat").asText().matches(legalSsn));
        }

        assertEquals(1, type1.elementSet().size());
        assertEquals(2, type2.elementSet().size());

        assertEquals(52, state1.elementSet().size());
        assertEquals(54, state2.elementSet().size());
    }

    @Test
    public void testBogusFieldLimit() throws IOException {
        try {
            SchemaSampler.fromResource("schema020.json");
            fail("Should have failed due to invalid fields");
        } catch (JsonMappingException e) {
            Preconditions.checkState(e.getCause() instanceof IllegalArgumentException, "Wrong exception");
        }

    }

}
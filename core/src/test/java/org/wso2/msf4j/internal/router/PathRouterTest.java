/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.msf4j.internal.router;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Test the routing logic using String as the destination.
 */
public class PathRouterTest {

    @Test
    public void testPathRoutings() {

        PatternPathRouter<String> pathRouter = PatternPathRouter.create();
        pathRouter.add("/", "root");
        pathRouter.add("/foo/{baz}/b", "foobarb");
        pathRouter.add("/foo/bar/baz", "foobarbaz");
        pathRouter.add("/baz/bar", "bazbar");
        pathRouter.add("/bar", "bar");
        pathRouter.add("/foo/bar", "foobar");
        pathRouter.add("//multiple/slash//route", "multipleslashroute");

        pathRouter.add("/multi/match/**", "multi-match-*");
        pathRouter.add("/multi/match/def", "multi-match-def");

        pathRouter.add("/multi/maxmatch/**", "multi-max-match-*");
        pathRouter.add("/multi/maxmatch/{id}", "multi-max-match-id");
        pathRouter.add("/multi/maxmatch/foo", "multi-max-match-foo");

        pathRouter.add("**/wildcard/{id}", "wildcard-id");
        pathRouter.add("/**/wildcard/{id}", "slash-wildcard-id");

        pathRouter.add("**/wildcard/**/foo/{id}", "wildcard-foo-id");
        pathRouter.add("/**/wildcard/**/foo/{id}", "slash-wildcard-foo-id");

        pathRouter.add("**/wildcard/**/foo/{id}/**", "wildcard-foo-id-2");
        pathRouter.add("/**/wildcard/**/foo/{id}/**", "slash-wildcard-foo-id-2");

        List<PatternPathRouter.RoutableDestination<String>> routes;

        routes = pathRouter.getDestinations("");
        assertEquals(1, routes.size());
        assertEquals("root", routes.get(0).getDestination());
        assertTrue(routes.get(0).getGroupNameValues().isEmpty());

        routes = pathRouter.getDestinations("/");
        assertEquals(1, routes.size());
        assertEquals("root", routes.get(0).getDestination());
        assertTrue(routes.get(0).getGroupNameValues().isEmpty());

        routes = pathRouter.getDestinations("/foo/bar/baz");
        assertEquals(1, routes.size());
        assertEquals("foobarbaz", routes.get(0).getDestination());
        assertTrue(routes.get(0).getGroupNameValues().isEmpty());

        routes = pathRouter.getDestinations("/baz/bar");
        assertEquals(1, routes.size());
        assertEquals("bazbar", routes.get(0).getDestination());
        assertTrue(routes.get(0).getGroupNameValues().isEmpty());

        routes = pathRouter.getDestinations("/foo/bar/baz/moo");
        assertTrue(routes.isEmpty());

        routes = pathRouter.getDestinations("/bar/121");
        assertTrue(routes.isEmpty());

        routes = pathRouter.getDestinations("/foo/bar/b");
        assertEquals(1, routes.size());
        assertEquals("foobarb", routes.get(0).getDestination());
        assertEquals(1, routes.get(0).getGroupNameValues().size());
        assertEquals("bar", routes.get(0).getGroupNameValues().get("baz"));

        routes = pathRouter.getDestinations("/foo/bar");
        assertEquals(1, routes.size());
        assertEquals("foobar", routes.get(0).getDestination());
        assertTrue(routes.get(0).getGroupNameValues().isEmpty());

        routes = pathRouter.getDestinations("/multiple/slash/route");
        assertEquals(1, routes.size());
        assertEquals("multipleslashroute", routes.get(0).getDestination());
        assertTrue(routes.get(0).getGroupNameValues().isEmpty());

        routes = pathRouter.getDestinations("/foo/bar/bazooka");
        assertTrue(routes.isEmpty());

        routes = pathRouter.getDestinations("/multi/match/def");
        assertEquals(2, routes.size());
        assertEquals(ImmutableSet.of("multi-match-def", "multi-match-*"),
                ImmutableSet.of(routes.get(0).getDestination(), routes.get(1).getDestination()));
        assertTrue(routes.get(0).getGroupNameValues().isEmpty());
        assertTrue(routes.get(1).getGroupNameValues().isEmpty());

        routes = pathRouter.getDestinations("/multi/match/ghi");
        assertEquals(1, routes.size());
        assertEquals("multi-match-*", routes.get(0).getDestination());
        assertTrue(routes.get(0).getGroupNameValues().isEmpty());

        routes = pathRouter.getDestinations("/multi/maxmatch/id1");
        assertEquals(2, routes.size());
        assertEquals(ImmutableSet.of("multi-max-match-id", "multi-max-match-*"),
                ImmutableSet.of(routes.get(0).getDestination(), routes.get(1).getDestination()));
        //noinspection assertEqualsBetweenInconvertibleTypes
        assertEquals(ImmutableSet.of(ImmutableMap.of("id", "id1"), ImmutableMap.<String, String>of()),
                ImmutableSet.of(routes.get(0).getGroupNameValues(), routes.get(1).getGroupNameValues())
        );

        routes = pathRouter.getDestinations("/multi/maxmatch/foo");
        assertEquals(3, routes.size());
        assertEquals(ImmutableSet.of("multi-max-match-id", "multi-max-match-*", "multi-max-match-foo"),
                ImmutableSet.of(routes.get(0).getDestination(), routes.get(1).getDestination(),
                        routes.get(2).getDestination()));
        //noinspection assertEqualsBetweenInconvertibleTypes
        assertEquals(ImmutableSet.of(ImmutableMap.of("id", "foo"), ImmutableMap.<String, String>of()),
                ImmutableSet.of(routes.get(0).getGroupNameValues(), routes.get(1).getGroupNameValues())
        );

        routes = pathRouter.getDestinations("/foo/bar/wildcard/id1");
        assertEquals(2, routes.size());
        assertEquals(ImmutableSet.of("wildcard-id", "slash-wildcard-id"),
                ImmutableSet.of(routes.get(0).getDestination(), routes.get(1).getDestination()));
        //noinspection assertEqualsBetweenInconvertibleTypes
        assertEquals(ImmutableSet.of(ImmutableMap.of("id", "id1"), ImmutableMap.<String, String>of("id", "id1")),
                ImmutableSet.of(routes.get(0).getGroupNameValues(), routes.get(1).getGroupNameValues())
        );

        routes = pathRouter.getDestinations("/wildcard/id1");
        assertEquals(1, routes.size());
        assertEquals("wildcard-id", routes.get(0).getDestination());
        assertEquals(ImmutableMap.of("id", "id1"), routes.get(0).getGroupNameValues());

        routes = pathRouter.getDestinations("/foo/bar/wildcard/bar/foo/id1");
        assertEquals(2, routes.size());
        assertEquals(ImmutableSet.of("wildcard-foo-id", "slash-wildcard-foo-id"),
                ImmutableSet.of(routes.get(0).getDestination(), routes.get(1).getDestination()));
        //noinspection assertEqualsBetweenInconvertibleTypes
        assertEquals(ImmutableSet.of(ImmutableMap.of("id", "id1"), ImmutableMap.<String, String>of("id", "id1")),
                ImmutableSet.of(routes.get(0).getGroupNameValues(), routes.get(1).getGroupNameValues())
        );

        routes = pathRouter.getDestinations("/foo/bar/wildcard/bar/foo/id1/baz/bar");
        assertEquals(2, routes.size());
        assertEquals(ImmutableSet.of("wildcard-foo-id-2", "slash-wildcard-foo-id-2"),
                ImmutableSet.of(routes.get(0).getDestination(), routes.get(1).getDestination()));
        //noinspection assertEqualsBetweenInconvertibleTypes
        assertEquals(ImmutableSet.of(ImmutableMap.of("id", "id1"), ImmutableMap.<String, String>of("id", "id1")),
                ImmutableSet.of(routes.get(0).getGroupNameValues(), routes.get(1).getGroupNameValues())
        );

        routes = pathRouter.getDestinations("/wildcard/bar/foo/id1/baz/bar");
        assertEquals(1, routes.size());
        assertEquals("wildcard-foo-id-2", routes.get(0).getDestination());
        assertEquals(ImmutableMap.of("id", "id1"), routes.get(0).getGroupNameValues());
    }
}

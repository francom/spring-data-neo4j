package org.springframework.test.snippets;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.RelationshipType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.conversion.ResultConverter;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.data.neo4j.template.Neo4jTemplate;
import org.springframework.test.DocumentingTestBase;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.neo4j.helpers.collection.MapUtil.map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:DocumentingTest-context.xml"})
public class SnippetNeo4jTemplateMethods extends DocumentingTestBase {
    @Autowired
    private GraphDatabase graphDatabase;
    private static final RelationshipType WORKS_WITH = DynamicRelationshipType.withName("WORKS_WITH");

    @Test
    @Transactional
    public void documentTemplateMethods() {
        title ="Basic operations";
        paragraphs = new String[] {"For direct retrieval of nodes and relationships, the <code>getReferenceNode()</code>,\n" +
                "           <code>getNode()</code> and <code>getRelationship()</code> methods can be used.",
                "There are methods (<code>createNode()</code> and <code>createRelationship()</code>) for creating nodes and\n" +
                        "           relationships that automatically set provided properties."};

        snippetTitle = "Neo4j template";
        snippet = "template";

        // SNIPPET template
        Neo4jOperations neo = new Neo4jTemplate(graphDatabase);

        Node mark = neo.createNode(map("name", "Mark"));
        Node thomas = neo.createNode(map("name", "Thomas"));

        neo.createRelationship(mark, thomas, WORKS_WITH, map("project", "spring-data"));

        neo.index("devs", thomas, "name", "Thomas");

        // Cypher
        assert "Mark".equals(neo.query("start p=({p_person}) match p<-[:WORKS_WITH]-other return other.name",
                map("person", thomas)).to(String.class).single());


        // SNIPPET template

        String thisShouldNotBePartOfTheSnippet;

        // SNIPPET template

        // Gremlin
        assert thomas.equals(neo.execute("g.v(person).out('WORKS_WITH')",
                map("person", mark)).to(Node.class).single());

        // Index lookup
        assert mark.equals(neo.lookup("devs", "name", "Mark").to(Node.class).single());

        // Index lookup with Result Converter
        assert "Mark".equals(neo.lookup("devs", "name", "Mark").to(String.class, new ResultConverter<PropertyContainer, String>() {
            public String convert(PropertyContainer element, Class<String> type) {
                return (String) element.getProperty("name");
            }
        }));
        // SNIPPET template
    }

}

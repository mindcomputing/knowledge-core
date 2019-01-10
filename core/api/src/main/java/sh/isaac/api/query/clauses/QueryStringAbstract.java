/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.api.query.clauses;

import java.util.UUID;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import sh.isaac.api.query.LeafClause;
import sh.isaac.api.query.LetItemKey;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.properties.QueryStringClause;

/**
 *
 * @author kec
 */
public abstract class QueryStringAbstract extends LeafClause implements QueryStringClause {
    public static final LetItemKey DEFAULT_QUERY_STRING_KEY 
            = new LetItemKey("Default query string key", 
                    UUID.fromString("dd11c59a-9afd-4a15-a4a9-32fb414f3299"));

    private LetItemKey queryStringKey = DEFAULT_QUERY_STRING_KEY;
    
    private boolean regex = false;

    public QueryStringAbstract() {
    }

    public QueryStringAbstract(Query enclosingQuery) {
        super(enclosingQuery);
    }

    public QueryStringAbstract(Query enclosingQuery, LetItemKey queryStringKey) {
        super(enclosingQuery);
        this.queryStringKey = queryStringKey;
    }
    
    @Override
    public final void setQueryStringKey(LetItemKey queryStringKey) {
        this.queryStringKey = queryStringKey;
    }

    @XmlElement
    @Override
    public final LetItemKey getQueryStringKey() {
        return this.queryStringKey;
    }    
    
    @Override
    public final String getQueryText() {
        return getLetItem(queryStringKey);
    }

    @XmlAttribute
    @Override
    public boolean isRegex() {
        return regex;
    }

    @Override
    public void setRegex(boolean regex) {
        this.regex = regex;
    }
    
    
}

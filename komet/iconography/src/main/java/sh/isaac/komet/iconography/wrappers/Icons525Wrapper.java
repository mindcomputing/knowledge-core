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
package sh.isaac.komet.iconography.wrappers;

import de.jensd.fx.glyphs.icons525.Icons525;
import java.io.InputStream;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

/**
 *
 * @author kec
 */
public class Icons525Wrapper extends GlyphFont {
    public static final String FONT_NAME = "525icons"; //$NON-NLS-1$


    /**
     * Do not call this constructor directly - instead access the
     * {@link FontAwesome.Glyph} public static enumeration method to create the glyph nodes), or
     * use the {@link GlyphFontRegistry} class to get access.
     *
     * Note: Do not remove this public constructor since it is used by the service loader!
     */
    public Icons525Wrapper() {
        this(Icons525.class.getResourceAsStream("525icons.ttf")); //$NON-NLS-1$
    }

    /**
     * Creates a new GlyphFont instance which uses the provided font source.
     * @param url
     */
    public Icons525Wrapper(String url){
        super(FONT_NAME, 14, url, true);
        for (Icons525 item: Icons525.values()) {
            register(item.name(), item.unicode().charAt(0));
        }
    }

    /**
     * Creates a new GlyphFont instance which uses the provided font source.
     * @param is
     */
    public Icons525Wrapper(InputStream is){
        super(FONT_NAME, 14, is, true);
        for (Icons525 item: Icons525.values()) {
            register(item.name(), item.unicode().charAt(0));
        }
    }

    
}

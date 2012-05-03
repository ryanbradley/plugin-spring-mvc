/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.forge.scaffold.spring.metawidget.widgetbuilder;

import static org.jboss.forge.scaffold.spring.metawidget.inspector.ForgeInspectionResultConstants.*;
import static org.metawidget.inspector.InspectionResultConstants.*;
import static org.metawidget.inspector.spring.SpringInspectionResultConstants.*;

import java.util.Collection;
import java.util.Map;

import org.jboss.forge.env.Configuration;
import org.jboss.forge.parser.java.util.Strings;
import org.jboss.forge.scaffold.spring.SpringScaffold;
import org.jvnet.inflector.Noun;
import org.metawidget.iface.MetawidgetException;
import org.metawidget.statically.BaseStaticXmlWidget;
import org.metawidget.statically.StaticXmlStub;
import org.metawidget.statically.StaticXmlWidget;
import org.metawidget.statically.html.widgetbuilder.HtmlInput;
import org.metawidget.statically.html.widgetbuilder.HtmlTableCell;
import org.metawidget.statically.jsp.StaticJspMetawidget;
import org.metawidget.statically.jsp.StaticJspUtils;
import org.metawidget.statically.jsp.widgetbuilder.CoreOut;
import org.metawidget.statically.jsp.widgetprocessor.StandardBindingProcessor;
import org.metawidget.statically.layout.SimpleLayout;
import org.metawidget.statically.spring.StaticSpringMetawidget;
import org.metawidget.statically.spring.widgetbuilder.FormOptionTag;
import org.metawidget.statically.spring.widgetbuilder.FormOptionsTag;
import org.metawidget.statically.spring.widgetbuilder.FormSelectTag;
import org.metawidget.statically.spring.widgetbuilder.SpringWidgetBuilder;
import org.metawidget.util.ClassUtils;
import org.metawidget.util.WidgetBuilderUtils;
import org.metawidget.util.simple.StringUtils;

/**
 * Builds widgets with Forge-specific behaviours (such as links to other Scaffolding pages)
 *
 * @author Ryan Bradley
 */

public class SpringEntityWidgetBuilder
        extends SpringWidgetBuilder
{
    //
    // Private statics
    //

    /**
     * Current Forge configuration.  Useful to retrieve <code>targetDir</code>.
     */

    private final Configuration config;

    //
    // Constructor
    //
    
    public SpringEntityWidgetBuilder(EntityWidgetBuilderConfig config)
    {
        this.config = config.getConfig();
    }

    //
    // Public methods
    //
    
    @Override
    public StaticXmlWidget buildWidget(String elementName, Map<String, String> attributes, StaticSpringMetawidget metawidget)
    {
        // Suppress nested INVERSE ONE_TO_ONE to avoid recursion.

        if (TRUE.equals(attributes.get(ONE_TO_ONE)) && TRUE.equals(attributes.get(INVERSE_RELATIONSHIP)))
        {
            return new StaticXmlStub();
        }

        String type = WidgetBuilderUtils.getActualClassOrType(attributes);

        Class<?> clazz = ClassUtils.niceForName(type);

        if (WidgetBuilderUtils.isReadOnly(attributes))
        {
            // Render read-only Spring lookup as a link.

            if (attributes.containsKey(SPRING_LOOKUP))
            {
                // (unless parent is already a link)

                if (widgetIsLink((BaseStaticXmlWidget) metawidget.getParent()))
                {
                    return null;
                }

                FormSelectTag select = new FormSelectTag();
                select.putAttribute("items", Noun.pluralOf(attributes.get(NAME)));
                select.putAttribute("path", attributes.get(NAME));

                String itemLabel = attributes.get(SPRING_LOOKUP_ITEM_LABEL);
                String itemValue = attributes.get(SPRING_LOOKUP_ITEM_VALUE);

                if (itemLabel != null)
                {
                    select.putAttribute("itemLabel", itemLabel);
                }

                if (itemValue != null)
                {
                    select.putAttribute("itemValue", itemValue);
                }

                if (TRUE.equals(attributes.get(REQUIRED)))
                {
                    FormOptionTag emptyOption = new FormOptionTag();
                    emptyOption.putAttribute("value", "");
                    select.getChildren().add(emptyOption);
                }

                String controllerName = Noun.pluralOf(clazz.getSimpleName()).toLowerCase();
                CoreUrl curl = new CoreUrl();
                curl.setValue(getTargetDir() + "/" + controllerName);
    
                HtmlAnchor link = new HtmlAnchor();
                link.putAttribute("href", curl.toString());
                link.putAttribute("value", "Create New " + StringUtils.uncamelCase(clazz.getSimpleName()));

                HtmlTableCell lookupCell = new HtmlTableCell();
                lookupCell.getChildren().add(select);
                lookupCell.getChildren().add(link);

                return lookupCell;
            }

            if (clazz != null)
            {
                // Render read-only booleans as graphics

                if (boolean.class.equals(clazz))
                {
                    CoreOut cout = new CoreOut();
                    StandardBindingProcessor processor = new StandardBindingProcessor();
                    processor.processWidget(cout, elementName, attributes, metawidget);

                    return cout;
                }
            }
        }

        // Render collection tables with links.

        // Render non-optional ONE_TO_ONE with a button.

        if (TRUE.equals(attributes.get(ONE_TO_ONE)))
        {
            // (we are about to create a nested metawidget, so we must prevent recursion)

            if (ENTITY.equals(elementName))
            {
                return null;
            }

            // Create nested StaticSpringMetawidget

            StaticSpringMetawidget nestedMetawidget = new StaticSpringMetawidget();
            String valueExpression = StaticJspUtils.unwrapExpression(((StaticJspMetawidget) metawidget).getValue());
            valueExpression += StringUtils.SEPARATOR_DOT_CHAR + attributes.get(NAME);
            ((StaticJspMetawidget) nestedMetawidget).setValue(valueExpression);
            metawidget.initNestedMetawidget(nestedMetawidget, attributes);

            // If read-only, we're done.

            if (WidgetBuilderUtils.isReadOnly(attributes))
            {
                return nestedMetawidget;
            }

            // Otherwise, use a dropdown menu with a create button.

            FormSelectTag select = new FormSelectTag();
            select.putAttribute("path", attributes.get(NAME) + ".id");

            if (!TRUE.equals(attributes.get(REQUIRED)))
            {
                FormOptionTag emptyOption = new FormOptionTag();
                emptyOption.putAttribute("value", "");
                select.getChildren().add(emptyOption);
                FormOptionsTag options = new FormOptionsTag();
                options.putAttribute("items", StaticJspUtils.wrapExpression(attributes.get(NAME)));
                options.putAttribute("itemValue", "id");
                select.getChildren().add(options);
            }
            else
            {
                select.putAttribute("items", StaticJspUtils.wrapExpression(Noun.pluralOf(attributes.get(NAME))));
                select.putAttribute("itemValue", "id");
            }

            HtmlTableCell cell = new HtmlTableCell();
            cell.getChildren().add(select);

            // TODO: Find a way to direct this link to a create form for the top entity, not the member.

            String controllerName = Noun.pluralOf(clazz.getSimpleName()).toLowerCase();
            CoreUrl curl = new CoreUrl();
            curl.setValue(getTargetDir() + controllerName + "/create");

            HtmlAnchor createLink = new HtmlAnchor();
            createLink.setTextContent("Create New " + StringUtils.uncamelCase(clazz.getSimpleName()));
            createLink.putAttribute("href", curl.toString());
            cell.getChildren().add(createLink);

            return cell;
        }

        if (clazz != null)
        {
            if (Collection.class.isAssignableFrom(clazz))
            {
                StaticJspMetawidget nestedMetawidget = new StaticJspMetawidget();
                nestedMetawidget.setInspector( metawidget.getInspector() );
                nestedMetawidget.setLayout( new SimpleLayout() );
                nestedMetawidget.setPath( metawidget.getPath() + StringUtils.SEPARATOR_FORWARD_SLASH_CHAR + attributes.get( NAME ) );
    
                // If using an external config, lookup StaticJspMetawidget within it
    
                if ( metawidget.getConfig() != null )
                {
                    nestedMetawidget.setConfig( metawidget.getConfig() );
                    try {
                        nestedMetawidget.getWidgetProcessors();
                    } catch ( MetawidgetException e ) {
                        nestedMetawidget.setConfig( null );
                    }
                }
    
                return nestedMetawidget;
            }
        }

        return null;
    }

    //
    // Private methods
    //

    private String getTargetDir()
    {
        String target = this.config.getString(SpringScaffold.class.getName() + "_targetDir");

        target = Strings.isNullOrEmpty(target) ? "" : target;

        if (!target.startsWith("/"))
            target = "/" + target;

        if (target.endsWith("/") && !target.startsWith("/"))
            target = target.substring(0, target.length()-1);

        return target;
    }

    private boolean widgetIsLink(BaseStaticXmlWidget widget)
    {
        if (widget instanceof HtmlAnchor)
        {
            return true;
        }

        if (widget instanceof HtmlInput && !widget.getAttribute("onclick").isEmpty())
        {
            return true;
        }

        return false;
    }
}

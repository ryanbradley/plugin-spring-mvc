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
package org.jboss.forge.scaffold.spring.metawidget.config;

import org.jboss.forge.env.Configuration;
import org.jboss.forge.project.Project;
import org.jboss.forge.scaffold.spring.util.AnnotationLookup;
import org.metawidget.config.impl.BaseConfigReader;

/**
 * ConfigReader with Forge-specific features.
 *
 * @author Richard Kennard
 */

public class ForgeConfigReader
         extends BaseConfigReader
{
   //
   // Private statics
   //

   private static final String CONFIG_ELEMENT_NAME = "forgeConfig";

   private static final String PROJECT_ELEMENT_NAME = "forgeProject";

   private static final String ANNOTATION_LOOKUP = "annotationLookup";

   //
   // Private members
   //

   private Configuration config;
   private Project project;
   private AnnotationLookup annotationLookup;

   //
   // Constructor
   //

   public ForgeConfigReader(Configuration config, Project project)
   {
      this.config = config;
      this.project = project;
      this.annotationLookup = new AnnotationLookup(project);
   }

   //
   // Protected methods
   //

   @Override
   protected boolean isNative(String name)
   {
      if (PROJECT_ELEMENT_NAME.equals(name) || ANNOTATION_LOOKUP.equals(name))
      {
         return true;
      }

      if (CONFIG_ELEMENT_NAME.equals(name))
      {
         return true;
      }

      return super.isNative(name);
   }

   @Override
   protected Object createNative(String name, Class<?> namespace, String recordedText) throws Exception
   {
      if (PROJECT_ELEMENT_NAME.equals(name))
      {
         return this.project;
      }
      if (ANNOTATION_LOOKUP.equals(name)) {
         return this.annotationLookup;
      }

      if(CONFIG_ELEMENT_NAME.equals(name))
      {
         return this.config;
      }

      return super.createNative(name, namespace, recordedText);
   }

}

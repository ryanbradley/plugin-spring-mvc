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
package org.jboss.forge.scaffold.spring.metawidget.inspector;

import org.jboss.forge.scaffold.spring.util.AnnotationLookup;
import org.metawidget.config.iface.NeedsResourceResolver;
import org.metawidget.config.iface.ResourceResolver;
import org.metawidget.inspector.impl.BaseObjectInspectorConfig;

/**
 *
 * @author Thomas Frühbeck
 */
public class ForgeInspectorConfig extends BaseObjectInspectorConfig implements NeedsResourceResolver {

   ResourceResolver resolver;
   AnnotationLookup annotationLookup;
   
   @Override
   public void setResourceResolver(ResourceResolver resourceResolver) {
      this.resolver = resourceResolver;
   }

   public ResourceResolver getResolver() {
      return resolver;
   }

   public void setAnnotationLookup(AnnotationLookup annotationLookup) {
      this.annotationLookup = annotationLookup;
   }

   public AnnotationLookup getAnnotationLookup() {
      return annotationLookup;
   }
   
}

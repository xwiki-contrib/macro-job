/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
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

package org.xwiki.contrib.jobmacro.internal.context;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.xpn.xwiki.web.XWikiServletRequestStub;

/**
 * Extends XWikiServletRequestStub to allow supports of query string parameters.
 *
 * @version $Id$
 */
public class XWikiServletRequestStubWithParameters extends XWikiServletRequestStub
{
    private final Map<String, String[]> parameters;

    XWikiServletRequestStubWithParameters(Map<String, String[]> parameters)
    {
        if (parameters != null && parameters.size() > 0) {
            Map<String, String[]> map = new HashMap<String, String[]>(parameters.size());
            for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
                int len = entry.getValue().length;
                String[] value = new String[entry.getValue().length];
                System.arraycopy(entry.getValue(), 0, value, 0, len);
                map.put(entry.getKey(), value);
            }
            this.parameters = Collections.unmodifiableMap(map);
        } else {
            this.parameters = Collections.<String, String[]>emptyMap();
        }
    }

    @Override
    public Map<String, String[]> getParameterMap()
    {
        return parameters;
    }

    @Override
    public String getParameter(String s)
    {
        String[] params = parameters.get(s);
        if (params != null && params.length > 0) {
            return params[0];
        }
        return null;
    }

    @Override
    public String[] getParameterValues(String s)
    {
        return parameters.get(s);
    }

    @Override
    public Enumeration<String> getParameterNames()
    {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String getCharacterEncoding()
    {
        return null;
    }
}

/**
 * Copyright (C) 2012 SINTEF <fabien@fleurey.com>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sensapp.android.sensappdroid.json;

import java.util.List;

public class SubscriptionJsonModel {

	private String sensor = null;
	private List<String> hooks = null;
	private String protocol = null;
    private String id = null;

	public SubscriptionJsonModel(String sensor, List<String> hooks, String protocol, String id) {
		this(sensor, hooks, protocol);
        this.id = id;
	}

    public SubscriptionJsonModel(String sensor, List<String> hooks, String protocol) {
        this(sensor, hooks);
        this.protocol = protocol;
    }

    public SubscriptionJsonModel(String sensor, List<String> hooks) {
        this.sensor = sensor;
        this.hooks = hooks;
    }

    public String getSensor() {
        return sensor;
    }
    public void setSensor(String sensor) {
        this.sensor = sensor;
    }
    public List<String> getHooks() {
        return hooks;
    }
    public void setHooks(List<String> hooks) {
        this.hooks = hooks;
    }
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}

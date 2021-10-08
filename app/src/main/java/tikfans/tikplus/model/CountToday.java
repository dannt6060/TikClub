/*
 *  Copyright 2016 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package tikfans.tikplus.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.SecureDate;

public class CountToday {
    public String today;
    public long count;
    public CountToday() {
        Date date = SecureDate.getInstance().getDate();
        DateFormat df;
        df = new SimpleDateFormat(AppUtil.FORMAT_DATE);
        today = df.format(date);
        count = 0;
    }

    public String getToday() {
        return today;
    }

    public void setToday(String today) {
        this.today = today;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}

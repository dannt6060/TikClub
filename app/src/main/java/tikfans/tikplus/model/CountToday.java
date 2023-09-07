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

import androidx.annotation.NonNull;

public class CountToday {
    public long sub;
    public long like;
    public Object time;
    public Object lastTime;


    public long getLike() {
        return like;
    }

    public void setLike(long like) {
        this.like = like;
    }

    public Object getTime() {
        return time;
    }

    public void setTime(Object time) {
        this.time = time;
    }

    public Object getLastTime() {
        return lastTime;
    }

    public void setLastTime(Object lastTime) {
        this.lastTime = lastTime;
    }

    public CountToday(long sub, long like, Object time, Object lastTime) {
        this.sub = sub;
        this.like = like;
        this.time = time;
        this.lastTime = lastTime;
    }

    public CountToday() {
        sub = 0;
        like = 0;
        time = 0L;
        lastTime = 0L;
    }

    public long getSub() {
        return sub;
    }

    public void setSub(long sub) {
        this.sub = sub;
    }
    @NonNull
    public String toString() {
        return "Sub: " + sub + " like: " + like;
    }
}

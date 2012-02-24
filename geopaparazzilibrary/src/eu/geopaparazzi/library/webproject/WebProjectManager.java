/*
 * Geopaparazzi - Digital field mapping on Android based devices
 * Copyright (C) 2010  HydroloGIS (www.hydrologis.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.geopaparazzi.library.webproject;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import eu.geopaparazzi.library.network.NetworkUtilities;
import eu.geopaparazzi.library.util.CompressionUtilities;
import eu.geopaparazzi.library.util.ResourcesManager;
import eu.geopaparazzi.library.util.debug.Debug;
import eu.geopaparazzi.library.util.debug.Logger;

/**
 * Singleton to handle cloud up- and download.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public enum WebProjectManager {
    INSTANCE;

    /**
     * Uploads a project folder as zip to the given server via POST.
     * 
     * @param context the {@link Context} to use.
     * @param addMedia defines if also the images in media should be included.
     * @param server the server to which to upload.
     * @param user the username for authentication.
     * @param passwd the password for authentication.
     * @return the return code.
     */
    public ReturnCodes uploadProject( Context context, boolean addMedia, String server, String user, String passwd ) {
        try {
            ResourcesManager resourcesManager = ResourcesManager.getInstance(context);
            File appFolder = resourcesManager.getApplicationDir();
            String mediaFodlerName = resourcesManager.getMediaDir().getName();

            File zipFile = new File(appFolder.getParentFile(), resourcesManager.getApplicationName() + ".zip"); //$NON-NLS-1$
            if (zipFile.exists()) {
                if (!zipFile.delete()) {
                    throw new IOException();
                }
            }
            if (addMedia) {
                CompressionUtilities.zipFolder(appFolder.getAbsolutePath(), zipFile.getAbsolutePath(), true);
            } else {
                CompressionUtilities.zipFolder(appFolder.getAbsolutePath(), zipFile.getAbsolutePath(), true, mediaFodlerName);
            }

            String result = NetworkUtilities.sendFilePost(server, zipFile, user, passwd);
            if (Debug.D) {
                Logger.i(this, result);
            }
            result = result.trim();
            if (result.equals("0")) { //$NON-NLS-1$
                return ReturnCodes.OK;
            } else {
                return ReturnCodes.ERROR;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnCodes.ERROR;
        }
    }

    /**
     * Downloads a project from the given server via GET.
     * 
     * @param context the {@link Context} to use.
     * @param server the server to which to upload.
     * @param user the username for authentication.
     * @param passwd the password for authentication.
     * @return the return code.
     */
    public ReturnCodes downloadProject( Context context, String server, String user, String passwd ) {
        try {
            ResourcesManager resourcesManager = ResourcesManager.getInstance(context);
            File appFolder = resourcesManager.getApplicationDir();

            File zipFile = new File(appFolder.getParentFile(), resourcesManager.getApplicationName() + ".zip"); //$NON-NLS-1$
            if (zipFile.exists()) {
                if (!zipFile.delete()) {
                    throw new IOException();
                }
            }

            NetworkUtilities.sendGetRequest4File(server, zipFile, null, user, passwd);

            // now remove the zip file
            CompressionUtilities.unzipFolder(zipFile.getAbsolutePath(), appFolder.getAbsolutePath());
            /*
             * remove the zip file
             */
            if (zipFile.exists()) {
                if (!zipFile.delete()) {
                    throw new IOException();
                }
            }

            return ReturnCodes.OK;
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnCodes.ERROR;
        }
    }

}
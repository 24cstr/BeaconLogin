package com.example.beaconlogin

import android.app.Activity

import android.os.RemoteException
import android.util.Log

import org.altbeacon.beacon.*

import org.altbeacon.beacon.Beacon


class BeaconMonitor : Activity(), BeaconConsumer, MonitorNotifier ,RangeNotifier{

var TAG ="BM"

    public override fun onResume() {
        try {
            super.onResume()
        }catch (E:Exception){}
        Log.d(
            TAG, "Resume "
        )
        MainActivity.mBeaconManager!!.bind(this)
    }

    override fun onBeaconServiceConnect() {

        // Set the two identifiers below to null to detect any beacon regardless of identifiers
        val myBeaconNamespaceId = Identifier.parse(MainActivity.BeaconNamespaceID)
        val myBeaconInstanceId = Identifier.parse(MainActivity.BeaconInstanceID)

        val region = Region("Unlock-Region", myBeaconNamespaceId, myBeaconInstanceId, null)
        MainActivity.mBeaconManager!!.addMonitorNotifier(this)
        try {
            MainActivity.mBeaconManager!!.startMonitoringBeaconsInRegion(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

        Log.d(
            TAG, "Looking for "+MainActivity.BeaconNamespaceID+" in region"
        )
    }

    override fun didEnterRegion(region: Region) {

        Log.d(
            TAG, "I detected a beacon in the region with namespace id " + region.id1 +
                    " and instance id: " + region.id2
        )
       // MainActivity.mBeaconManager?.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))
      //  MainActivity.mBeaconManager?.addRangeNotifier(this)
    }

    override fun didExitRegion(region: Region) {

        Log.d(
            TAG, "I exited the region of  beacon " + region.id1 +
                    " and instance id: " + region.id2
        )

    }

    override fun didRangeBeaconsInRegion(beacons: Collection<Beacon>, region: Region) {
        for (beacon in beacons) {
            if (beacon.distance < 6) {
                Log.d(TAG, "I see a beacon "+beacon.toString()+" that is less than "+beacon.distance+" meters away.")
                MainActivity.loginbut.isEnabled = true
            }
            else {
                Log.d(TAG, "going Outside region")
                MainActivity.loginbut.isEnabled = false
                if(MainActivity.loginFlag)
                MainActivity.getmInstanceActivity()?.signOut()
            }
        }
    }

    override fun didDetermineStateForRegion(state: Int, region: Region) {
        MainActivity.mBeaconManager?.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))
        MainActivity.mBeaconManager?.addRangeNotifier(this)
    }

    public override fun onPause() {
        super.onPause()
        MainActivity.mBeaconManager!!.unbind(this)
    }
}
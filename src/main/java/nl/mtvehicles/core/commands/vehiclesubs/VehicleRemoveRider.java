package nl.mtvehicles.core.commands.vehiclesubs;

import nl.mtvehicles.core.events.VehicleRemoveRiderEvent;
import nl.mtvehicles.core.infrastructure.enums.Message;
import nl.mtvehicles.core.infrastructure.models.MTVehicleSubCommand;
import nl.mtvehicles.core.infrastructure.models.Vehicle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * <b>/vehicle removerider %player%</b> - remove a player who may steer the vehicle the player is sitting in (if they are its owner) OR player's held vehicle.
 */
public class VehicleRemoveRider extends MTVehicleSubCommand {
    public VehicleRemoveRider() {
        this.setPlayerCommand(true);
    }

    @Override
    public boolean execute() {

        Vehicle vehicle = getVehicle();
        if (vehicle == null) return true;

        if (arguments.length != 2) {
            sendMessage(Message.USE_REMOVE_RIDER);
            return true;
        }

        Player argPlayer = Bukkit.getPlayer(arguments[1]);

        VehicleRemoveRiderEvent api = new VehicleRemoveRiderEvent();
        api.setPlayer(player);
        api.setRemoved(argPlayer);
        api.setLicensePlate(vehicle.getLicensePlate());
        api.call();

        if (api.isCancelled()) return true;
        vehicle = api.getVehicle();
        argPlayer = api.getRemoved();

        if (vehicle == null){
            sendMessage(Message.VEHICLE_NOT_FOUND);
            return true;
        }

        if (argPlayer == null) {
            sendMessage(Message.PLAYER_NOT_FOUND);
            return true;
        }

        List<String> riders = vehicle.getRiders();
        String playerUUID = argPlayer.getUniqueId().toString();

        if (!riders.contains(playerUUID)){
            sendMessage(Message.NOT_A_RIDER);
            return true;
        }

        riders.remove(playerUUID);
        vehicle.setRiders(riders);
        vehicle.save();
        sendMessage(Message.MEMBER_CHANGE);

        return true;
    }
}

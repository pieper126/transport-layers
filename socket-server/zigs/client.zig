const std = @import("std");

pub fn main() !void {
    const address = try std.net.Address.resolveIp("127.0.0.1", 8080);
    const connection = try std.net.tcpConnectToAddress(address);

    const reader = connection.reader();
    const writer = connection.writer();

    var buffer: [1024]u8 = undefined;
    const message = "{ \"json\": \"test\" }";

    while (true) {
        try writer.writeAll(message);
        _ = try reader.readAll(&buffer);
        std.debug.print("{s}", .{buffer});
    }
}

const std = @import("std");

pub fn main() !void {
    const address = try std.net.Address.resolveIp("127.0.0.1", 8080);
    const connection = try std.net.tcpConnectToAddress(address);

    const reader = connection.reader();
    const writer = connection.writer();

    var buffer: [1024]u8 = undefined;
    @memset(&buffer, 0);

    const message = "{ \"json\": \"test\" }";

    for (0..100) |_| {
        try writer.writeAll(message);
        _ = try reader.read(&buffer);
        std.debug.print("{s}", .{buffer});
    }

    connection.close();

    return;
}

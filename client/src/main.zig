const std = @import("std");
const curl = @import("curl");

pub fn main() !void {
    var gpa = std.heap.GeneralPurposeAllocator(.{}){};
    const allocator = gpa.allocator();
    defer _ = gpa.deinit();

    //try optimized_post(allocator);
    try try_curl(allocator);
}

fn try_curl(allocator: std.mem.Allocator) !void {
    const easy = try curl.Easy.init(allocator, .{});
    defer easy.deinit();
    const payload = "{\"json\": \"test\" }";

    for (0..100_000) |_| {
        const resp = try easy.post("http://localhost:3000/echo/", "application/json", payload);
        defer resp.deinit();
        _ = resp.body.?.items;
    }
}

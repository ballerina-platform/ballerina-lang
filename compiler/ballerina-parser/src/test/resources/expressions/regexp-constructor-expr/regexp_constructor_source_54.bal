function testInvalidRegex() {
    _ = re `\p{Lz}`;
    _ = re `\p{kLz}`;
    _ = re `\p{L2z}`;

    _ = re `\p{Mz}`;
    _ = re `\p{kMz}`;
    _ = re `\p{M2z}`;

    _ = re `\p{Nz}`;
    _ = re `\p{kNz}`;
    _ = re `\p{N2z}`;

    _ = re `\p{Sz}`;
    _ = re `\p{kSz}`;
    _ = re `\p{S2z}`;

    _ = re `\p{Pz}`;
    _ = re `\p{kPz}`;
    _ = re `\p{P2z}`;

    _ = re `\p{Zt}`;
    _ = re `\p{kZt}`;
    _ = re `\p{Z2t}`;

    _ = re `\p{Cz}`;
    _ = re `\p{kCz}`;
    _ = re `\p{C2z}`;

    _ = re `\p{L!!`;
    _ = re `\p{kLabcd.!;{`;
}

public class IPv4Address {
    private String addressString;
    private long address;

    private long ipToLong(String address) throws IllegalArgumentException {
        String[] octets = address.split("\\.");
        long result = 0;

        for ( int i = 3, index = 0; i >= 0; i--, index++ ) {
            long octet = Long.parseLong(octets[index]);

            if ( octet < 0 || octet > 255 ) {
                throw new IllegalArgumentException(Long.toString(octet));
            }

            result |= octet << (i * 8);
        }
        return result;
    }

    private String ipToString(long address) throws IllegalArgumentException {
        if ( address < 0 || address > 0xFFFFFFFFL ) {
            throw new IllegalArgumentException(Long.toString(address));
        }

        return String.format("%d.%d.%d.%d",
                (address >> 24) & 0xFF, (address >> 16) & 0xFF, (address >> 8) & 0xFF, address & 0xFF);
    }

    public IPv4Address(String address) throws IllegalArgumentException {
        this.address = this.ipToLong(address);
        this.addressString = address;
    }

    public IPv4Address(long address) throws IllegalArgumentException {
        this.addressString = this.ipToString(address);
        this.address = address;
    }

    public boolean lessThan(IPv4Address address) {
        return this.address < address.toLong();
    }

    public boolean greaterThan(IPv4Address address) {
        return this.address > address.toLong();
    }

    public boolean equals(IPv4Address address) {
        return this.address == address.toLong();
    }

    public String toString() {
        return this.addressString;
    }

    public long toLong() {
        return this.address;
    }
}


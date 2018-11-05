public class Network {
    private IPv4Address address;
    private IPv4Address broadcastAddress;
    private IPv4Address firstUsableAddress;
    private IPv4Address lastUsableAddress;
    private int maskLength;
    private long mask;
    private String maskString;
    private long totalHosts;
    private long wildcard;

    private static final long MASK_32 = 0xFFFFFFFFL;
    private static final Network[] privateNets = new Network[] {
            new Network(new IPv4Address("10.0.0.0"), 8),
            new Network(new IPv4Address("172.16.0.0"), 12),
            new Network(new IPv4Address("192.168.0.0"), 16)
    };

    private void maskValidate() throws IllegalArgumentException {
        if ( maskLength < 0 || maskLength > 32 ) {
            throw new IllegalArgumentException(Integer.toString(maskLength));
        }
    }

    private void totalHostsValidate() {
        if ( this.totalHosts < 0 ) {
            this.totalHosts = 0;
        }
    }

    public Network(IPv4Address address, int maskLength) throws IllegalArgumentException {
        this.maskValidate();
        this.wildcard = MASK_32 >> maskLength;
        this.mask = this.wildcard ^ MASK_32;
        this.address = new IPv4Address(address.toLong() & this.mask);
        this.maskLength = maskLength;
        this.totalHosts = this.wildcard - 1;
        this.totalHostsValidate();
        this.firstUsableAddress = null;
        this.lastUsableAddress = null;
        this.broadcastAddress =null;
        this.maskString =null;
    }

    public boolean contains(IPv4Address address) {
        return (address.toLong() & this.mask) == this.address.toLong();
    }

    public IPv4Address getAddress() {
        return this.address;
    }

    public IPv4Address getBroadcastAddress() {
        if ( this.broadcastAddress == null ) {
            this.broadcastAddress = new IPv4Address(this.address.toLong() | this.wildcard);
        }

        return this.broadcastAddress;
    }

    public IPv4Address getFirstUsableAddress() {
        if ( this.firstUsableAddress == null ) {
            if ( this.maskLength >= 31 ) {
                this.firstUsableAddress = new IPv4Address(this.address.toLong());
            } else {
                this.firstUsableAddress = new IPv4Address(this.address.toLong() + 1);
            }
        }

        return  this.firstUsableAddress;
    }

    public IPv4Address getLastUsableAddress() {
        if ( this.lastUsableAddress == null ) {
            if ( this.maskLength >= 31 ) {
                this.lastUsableAddress = new IPv4Address(this.address.toLong());
            } else {
                this.lastUsableAddress = new IPv4Address(this.address.toLong() + this.totalHosts);
            }
        }

        return this.lastUsableAddress;
    }

    public long getMask() {
        return this.mask;
    }

    public String getMaskString() {
        if ( this.maskString == null ) {
            this.maskString = String.format("%d.%d.%d.%d",
                    (this.mask >> 24) & 0xFF, (this.mask >> 16) & 0xFF, (this.mask >> 8) & 0xFF, this.mask & 0xFF);
        }

        return this.maskString;
    }

    public int getMaskLength() {
        return this.maskLength;
    }

    public Network[] getSubnets() throws Exception {
        if ( this.mask == MASK_32 ) {
            throw new Exception("Impossible get subnets, mask length is 32");
        }

        int newMaskLength = this.maskLength + 1;
        Network[] subnets = new Network[2];

        subnets[0] = new Network(new IPv4Address(this.address.toLong()), newMaskLength);
        subnets[1] = new Network(new IPv4Address(subnets[0].getBroadcastAddress().toLong() + 1), newMaskLength);

        return subnets;
    }

    public long getTotalHosts() {
        return this.totalHosts;
    }

    public boolean isPublic() {
        for ( Network net: privateNets ) {
            if ( net.contains(this.address) ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        out./*append("   Network: ").*/append(this.address).append("/").append(this.maskLength)/*.append("\n")*/;
//        out.append("      Mask: ").append(this.getMaskString()).append("\n");
//        out.append("    Prefix: ").append(this.maskLength).append("\n");
//        out.append("     First: ").append(this.getFirstUsableAddress()).append("\n");
//        out.append("      Last: ").append(this.getLastUsableAddress()).append("\n");
//        out.append(" Broadcast: ").append(this.getBroadcastAddress()).append("\n");
//        out.append("TotalHosts: ").append(this.totalHosts).append("\n");
//        out.append("  IsPublic: ").append(this.isPublic());

        return out.toString();
    }
}

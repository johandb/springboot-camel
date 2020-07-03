pragma solidity ^0.5.16;

contract Oracle {
    // Contract owner
    address public owner;

    // BTC Marketcap Storage
    uint public btcMarketCap;

    struct ConsentGiven{
        address signer; // Notary
        uint date; // Date of notarization
        string hash; // Student Hash
        bool consent; // Parental consent
    }

    /**
     *  @dev Storage space used to record all consents notarized with metadata
     */
    mapping(bytes32 => ConsentGiven ) registry;

    // Callback function
    event CallbackGetBTCCap();
	
    /**
     *  @dev Definition of the event triggered when a consent is successfully notarized in the registry
     */
	event Notarized(address indexed _signer, string _studentHash);

    constructor() public {
        owner = msg.sender;
    }

    function updateBTCCap() public {
        // Calls the callback function
        emit CallbackGetBTCCap();
    }

    function setBTCCap(uint cap) public {
        // If it isn't sent by a trusted oracle
        // a.k.a ourselves, ignore it
        require(msg.sender == owner);
        btcMarketCap = cap;
    }

    function getBTCCap() view public returns (uint) {
        return btcMarketCap;
    }
	
    /**
     *  @dev Notarize a consent identified by the hash of the consent hash, the sender and date in the registry
     *  @dev Emit an event Notarized in case of success
     *  @param _studentHash Consent hash
     */
    function setConsent(string calldata _studentHash, bool _consent) external returns (bool) {
        bytes32 id = keccak256(abi.encodePacked(_studentHash));

        registry[id].signer = msg.sender;
        registry[id].date = now;
        registry[id].hash = _studentHash;
        registry[id].consent = _consent;

        emit Notarized(msg.sender, _studentHash);

        return true;
    }
	
    /**
     *  @dev Verify a consent identified by its has was noterized in the registry previsouly.
     *  @param _studentHash Consent hash
     *  @return bool if consent was noterized previsouly in the registry
     */
    function getConsent(string calldata _studentHash) external view returns (bool) {
        return registry[keccak256(abi.encodePacked(_studentHash))].consent;
    }
	
}



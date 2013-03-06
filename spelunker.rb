require 'sinatra/base'
require 'json'
require 'sinatra/synchrony'
require './data_sources/sky_server'
require './lib/sinatra/redis_cache'

class Spelunker < Sinatra::Base
  register Sinatra::Synchrony
  register Sinatra::RedisCache

  get '/pingdom' do
    'ok'
  end

  get '/sky_server' do
    spec = params.has_key? 'spec'

    if (params.has_key? 'ra' ) && (params.has_key? 'dec' ) && (params.has_key? 'radius')
      ra, dec = params.values_at('ra', 'dec').map{|val| val.to_f.round(2)}
      limit = params['limit']
      limit ||= 100

      data = cache "#{ra}-#{dec}-#{params['radius']}-#{limit}-#{spec}" do
        SkyServer.by_ra_dec ra: ra, dec: dec, 
                            limit: limit, radius: params['radius'], spec: spec
      end

    elsif (params.has_key? 'tolerance')
      u, g, r, i, z = params.values_at('u', 'g', 'r', 'i', 'z')
      limit = params['limit']
      limit ||= 100
      tolerance = params['tolerance'].to_f

      data = cache "#{u}-#{g}-#{r}-#{i}-#{z}-#{tolerance}-#{limit}-#{spec}" do 
        SkyServer.by_ugriz u: u, g: g, r: r, i: i, z: z, 
                           tolerance: tolerance, limit: limit, spec: spec
      end
    elsif (params.has_key? 'query')
      data = SkyServer.query params['query']
    end

    if data.is_a? String
      status 404
      body({ status: data }.to_json)
    elsif data.nil?
      status 401
      body({ status: "Bad Parameters" }.to_json)
    else
      status 200
      body data.to_json
    end
  end

  options '*' do
    status 200
  end

  before '*' do
    response['Access-Control-Allow-Origin'] = '*'
    response['Access-Control-Allow-Methods'] = 'GET, OPTIONS'
    response['Access-Control-Allow-Headers'] = %w(Origin Accept Content-Type X-Requested-With X-CSRF-Token Authorization)
    content_type 'application/json'
  end
end